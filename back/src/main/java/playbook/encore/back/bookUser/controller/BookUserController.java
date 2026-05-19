package playbook.encore.back.bookUser.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.*;

import playbook.encore.back.bookUser.dto.*;
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
    public ResponseEntity<RegisterUserResponseDto> registerUser(@RequestBody RegisterUserRequestDto registerUserRequestDto) throws Exception {
        RegisterUserResponseDto registerUserResponseDto = bookUserService.createUser(registerUserRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(registerUserResponseDto);
    }

    @GetMapping("/register/validate")
    public ResponseEntity<RegisterIdValidateResponseDto> validationId(@RequestParam("id") String idUser) throws Exception {
        RegisterIdValidateResponseDto registerIdValidateResponseDto = bookUserService.checkUserId(idUser);
        return ResponseEntity.status(HttpStatus.OK).body(registerIdValidateResponseDto);
    }

    // 로그인 관련 부분
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            HttpServletRequest request,
            @RequestBody LoginUserRequestDto loginUserRequestDto) throws Exception {
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
            return ResponseEntity.status(HttpStatus.OK).body("로그인 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.status(HttpStatus.OK).body("로그아웃 성공");
    }

    // 회원정보 관련 부분
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
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
            return ResponseEntity.status(HttpStatus.OK).body(loginUserDataResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
    }

    @PostMapping("/validate")
    public ResponseEntity<?> getCurrentPassword(
            HttpServletRequest request,
            @RequestBody PasswordValidateRequestDto requestDto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.validatePassword(user, requestDto.getPassword());
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            HttpServletRequest request,
            @RequestBody PasswordUpdateRequestDto requestDto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.updatePassword(user, requestDto.getNewPassword());
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getUserList(
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
                return ResponseEntity.status(HttpStatus.OK).body(bookUserService.getBookUserList(campusId));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(
            HttpServletRequest request,
            @RequestBody(required = false) DeleteUserRequestDto deleteUserRequestDto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");

            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                if (deleteUserRequestDto == null || deleteUserRequestDto.getIdUser() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("삭제할 사용자 ID가 필요합니다.");
                }
                boolean result = bookUserService.deleteUserByAdmin(deleteUserRequestDto.getIdUser());
                return ResponseEntity.status(HttpStatus.OK).body(result);
            } else if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.deleteUserBySelf(user);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
