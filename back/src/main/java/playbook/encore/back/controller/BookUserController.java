package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import playbook.encore.back.data.dto.bookUser.*;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.BookUserService;
import playbook.encore.back.data.entity.BookUser;

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
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }    
    }

    // 회원정보 관련 부분
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
            BookUser user = (BookUser) request.getAttribute("user");
            LoginUserDataResponseDto loginUserDataResponseDto = new LoginUserDataResponseDto(user.getSeqCourse().getSeqCourse(), user.getIdUser(), user.getNameUser(), user.getDcUser());
            return ResponseEntity.status(HttpStatus.OK).body(loginUserDataResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
    }

    @PostMapping("/validate")
    public ResponseEntity<?> getCurrentPassword(
            HttpServletRequest request,
            @RequestBody String password
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.validatePassword(user, password);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            HttpServletRequest request,
            @RequestBody String newPassword
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.updatePassword(user, newPassword);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
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
    @PutMapping("/course")
    public ResponseEntity<?> updateCourse(
            HttpServletRequest request,
            @RequestBody Integer newSeqCourse
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
                BookUser user = (BookUser) request.getAttribute("user");
                boolean result = bookUserService.updateCourse(user, newSeqCourse);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("유저만 접근 가능합니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getUserList(
            HttpServletRequest request
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                return ResponseEntity.status(HttpStatus.OK).body(bookUserService.getBookUserList());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(
            HttpServletRequest request,
            @RequestBody String idUser
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                boolean result = bookUserService.deleteUserByAdmin(idUser);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }
}
