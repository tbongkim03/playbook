package playbook.encore.back.bookUser.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import playbook.encore.back.bookUser.dto.*;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.bookUser.service.BookUserService;
import playbook.encore.back.bookUser.entity.BookUser;

@RestController
@RequestMapping("/users")
public class BookUserController {
    
    private final BookUserService bookUserService;

    @Autowired
    public BookUserController(BookUserService bookUserService) {
        this.bookUserService = bookUserService;
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
    public ResponseEntity<?> loginUser(@RequestBody LoginUserRequestDto loginUserRequestDto) throws Exception {
        try {
            LoginUserResponseDto loginUserResponseDto = bookUserService.loginServiceUser(loginUserRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(loginUserResponseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }    
    }

    // 회원정보 관련 부분
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
            BookUser user = (BookUser) request.getAttribute("user");
            
            // seqCourse가 null인 경우 처리 (과정이 삭제된 경우)
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
//    @PutMapping("/discord")
//    public ResponseEntity<?> updateDiscord(
//            HttpServletRequest request,
//            @RequestBody String newDiscord
//    ) throws Exception {
//        try {
//            Object roleAttr = request.getAttribute("ROLE");
//            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
//                BookUser user = (BookUser) request.getAttribute("user");
//                boolean result = bookUserService.updateDiscord(user, newDiscord);
//                return ResponseEntity.status(HttpStatus.OK).body(result);
//            }
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
//        } catch (Exception IllegalArgumentException) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
//        }
//    }
//    @PutMapping("/course")
//    public ResponseEntity<?> updateCourse(
//            HttpServletRequest request,
//            @RequestBody Integer newSeqCourse
//    ) throws Exception {
//        try {
//            Object roleAttr = request.getAttribute("ROLE");
//            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
//                BookUser user = (BookUser) request.getAttribute("user");
//                boolean result = bookUserService.updateCourse(user, newSeqCourse);
//                return ResponseEntity.status(HttpStatus.OK).body(result);
//            }
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
//        } catch (Exception IllegalArgumentException) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
//        }
//    }

    @GetMapping("/list")
    public ResponseEntity<?> getUserList(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                // 쿼리 파라미터로 전달된 campusId가 있으면 우선 사용
                Integer campusId = requestCampusId;
                
                // 쿼리 파라미터가 없으면 interceptor에서 설정한 campusId 사용
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
                // 관리자가 다른 사용자 삭제
                if (deleteUserRequestDto == null || deleteUserRequestDto.getIdUser() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("삭제할 사용자 ID가 필요합니다.");
                }
                boolean result = bookUserService.deleteUserByAdmin(deleteUserRequestDto.getIdUser());
                return ResponseEntity.status(HttpStatus.OK).body(result);
            } else if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                // 사용자 본인 탈퇴
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
