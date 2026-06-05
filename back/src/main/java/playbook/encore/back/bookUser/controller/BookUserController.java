package playbook.encore.back.bookUser.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.*;

import playbook.encore.back.bookUser.dto.*;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseCode;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.bookUser.service.BookUserService;
import playbook.encore.back.bookUser.entity.BookUser;

import org.springframework.context.ApplicationEventPublisher;
import playbook.encore.back.accesslog.event.LoginEvent;
import playbook.encore.back.common.excel.ExcelUtil;
import playbook.encore.back.common.exception.NotAuthorizedException;
import playbook.encore.back.common.util.AuthUtil;
import playbook.encore.back.common.util.SessionUtil;
import playbook.encore.back.common.util.WebUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
public class BookUserController {

    private final BookUserService bookUserService;
    private final RedisIndexedSessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public BookUserController(BookUserService bookUserService,
                              RedisIndexedSessionRepository sessionRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.bookUserService = bookUserService;
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
    }

    // 회원가입 관련 부분
    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody @Valid RegisterUserRequestDto registerUserRequestDto) throws Exception {
        RegisterUserResponseDto registerUserResponseDto = bookUserService.createUser(registerUserRequestDto);
        return ResponseEntity.ok(ResponseHandler.success(registerUserResponseDto));
    }

    @GetMapping("/register/validate")
    public ResponseEntity<Response> validationId(@RequestParam("id") String idUser) throws Exception {
        RegisterIdValidateResponseDto registerIdValidateResponseDto = bookUserService.checkUserId(idUser);
        return ResponseEntity.ok(ResponseHandler.success(registerIdValidateResponseDto));
    }

    // 로그인 관련 부분
    @PostMapping("/login")
    public ResponseEntity<Response> loginUser(
            HttpServletRequest request,
            @RequestBody @Valid LoginUserRequestDto loginUserRequestDto) throws Exception {
        String ip = WebUtil.getClientIp(request);
        try {
            BookUser user = bookUserService.loginServiceUser(loginUserRequestDto);
            SessionUtil.createSession(request, sessionRepository, user.getIdUser(), "user");
            eventPublisher.publishEvent(LoginEvent.success("USER", user.getSeqUser().longValue(), user.getIdUser(), ip));
            return ResponseEntity.ok(ResponseHandler.success());
        } catch (IllegalArgumentException e) {
            eventPublisher.publishEvent(LoginEvent.fail("USER", loginUserRequestDto.getIdUser(), ip, e.getMessage()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseHandler.error(ResponseCode.NOT_AUTHENTICATED, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ResponseHandler.success());
    }

    // 회원정보 관련 부분
    @GetMapping("/me")
    public ResponseEntity<Response> getUserInfo(HttpServletRequest request) {
        BookUser user = AuthUtil.getUser(request);

        Integer seqCourse = null;
        Integer seqCampus = null;
        String campusName = null;

        if (user.getSeqCourse() != null) {
            seqCourse = user.getSeqCourse().getSeqCourse();
            if (user.getSeqCourse().getSeqCampus() != null) {
                seqCampus = user.getSeqCourse().getSeqCampus().getSeqCampus();
                campusName = user.getSeqCourse().getSeqCampus().getNameCampus();
            }
        }

        LoginUserDataResponseDto loginUserDataResponseDto = new LoginUserDataResponseDto(
            seqCourse,
            seqCampus,
            campusName,
            user.getIdUser(),
            user.getNameUser(),
            user.getDcUser(),
            user.getStatusUser().toString()
        );
        return ResponseEntity.ok(ResponseHandler.success(loginUserDataResponseDto));
    }

    @PostMapping("/validate")
    public ResponseEntity<Response> getCurrentPassword(
            HttpServletRequest request,
            @RequestBody @Valid PasswordValidateRequestDto requestDto
    ) throws Exception {
        BookUser user = AuthUtil.getUser(request);
        boolean result = bookUserService.validatePassword(user, requestDto.getPassword());
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PutMapping("/update")
    public ResponseEntity<Response> updateUser(
            HttpServletRequest request,
            @RequestBody UpdateUserRequestDto dto
    ) throws Exception {
        BookUser user = AuthUtil.getUser(request);
        boolean result = bookUserService.updateUser(user, dto);
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PutMapping("/password")
    public ResponseEntity<Response> updatePassword(
            HttpServletRequest request,
            @RequestBody @Valid PasswordUpdateRequestDto requestDto
    ) throws Exception {
        BookUser user = AuthUtil.getUser(request);
        boolean result = bookUserService.updatePassword(user, requestDto.getNewPassword());
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PutMapping("/admin/reset-password")
    public ResponseEntity<Response> resetUserPassword(
            HttpServletRequest request,
            @RequestBody @Valid ResetUserPasswordRequestDto dto
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        boolean result = bookUserService.resetUserPassword(dto.getIdUser(), dto.getNewPassword());
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @GetMapping("/list")
    public ResponseEntity<Response> getUserList(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        Integer campusId = AuthUtil.getCampusId(request, requestCampusId);
        return ResponseEntity.ok(ResponseHandler.success(bookUserService.getBookUserList(campusId)));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        Integer campusId = AuthUtil.getCampusId(request, requestCampusId);
        byte[] data = bookUserService.exportExcel(campusId);
        return ExcelUtil.toResponse(data, "학생계정");
    }

    @DeleteMapping
    public ResponseEntity<Response> deleteUser(
            HttpServletRequest request,
            @RequestBody(required = false) DeleteUserRequestDto deleteUserRequestDto
    ) throws Exception {
        if (AuthUtil.isAdmin(request)) {
            if (deleteUserRequestDto == null || deleteUserRequestDto.getIdUser() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam("idUser"));
            }
            boolean result = bookUserService.deleteUserByAdmin(deleteUserRequestDto.getIdUser());
            return ResponseEntity.ok(ResponseHandler.success(result));
        } else if (AuthUtil.isUser(request)) {
            BookUser user = (BookUser) request.getAttribute("user");
            boolean result = bookUserService.deleteUserBySelf(user);
            return ResponseEntity.ok(ResponseHandler.success(result));
        }
        throw new NotAuthorizedException();
    }
}
