package playbook.encore.back.bookUser.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.*;

import playbook.encore.back.bookUser.dto.*;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseCode;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.bookUser.service.BookUserService;
import playbook.encore.back.bookUser.entity.BookUser;


@RestController
@RequestMapping("/users")
public class BookUserController {

    private final BookUserService bookUserService;
    private final RedisIndexedSessionRepository sessionRepository;

    @Autowired
    public BookUserController(BookUserService bookUserService,
                              RedisIndexedSessionRepository sessionRepository) {
        this.bookUserService = bookUserService;
        this.sessionRepository = sessionRepository;
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
        try {
            String userId = bookUserService.loginServiceUser(loginUserRequestDto);

            // 기존 세션 만료 (중복 로그인 방지)
            sessionRepository.findByIndexNameAndIndexValue(
                    FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, userId)
                    .keySet().forEach(sessionRepository::deleteById);

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", userId);
            session.setAttribute("role", "user");
            session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, userId);
            session.setMaxInactiveInterval(3600);
            return ResponseEntity.ok(ResponseHandler.success());
        } catch (IllegalArgumentException e) {
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
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
            BookUser user = (BookUser) request.getAttribute("user");

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
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
    }

    @PostMapping("/validate")
    public ResponseEntity<Response> getCurrentPassword(
            HttpServletRequest request,
            @RequestBody @Valid PasswordValidateRequestDto requestDto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.validatePassword(user, requestDto.getPassword());
                return ResponseEntity.ok(ResponseHandler.success(result));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<Response> updatePassword(
            HttpServletRequest request,
            @RequestBody @Valid PasswordUpdateRequestDto requestDto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.updatePassword(user, requestDto.getNewPassword());
                return ResponseEntity.ok(ResponseHandler.success(result));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Response> getUserList(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Integer campusId = requestCampusId;
                if (campusId == null) {
                    campusId = (Integer) request.getAttribute("campusId");
                }
                return ResponseEntity.ok(ResponseHandler.success(bookUserService.getBookUserList(campusId)));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @DeleteMapping
    public ResponseEntity<Response> deleteUser(
            HttpServletRequest request,
            @RequestBody(required = false) DeleteUserRequestDto deleteUserRequestDto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");

            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                if (deleteUserRequestDto == null || deleteUserRequestDto.getIdUser() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam("idUser"));
                }
                boolean result = bookUserService.deleteUserByAdmin(deleteUserRequestDto.getIdUser());
                return ResponseEntity.ok(ResponseHandler.success(result));
            } else if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.deleteUserBySelf(user);
                return ResponseEntity.ok(ResponseHandler.success(result));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }
}
