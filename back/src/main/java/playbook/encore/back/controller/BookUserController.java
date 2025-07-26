package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.service.BookService;
import playbook.encore.back.service.BookUserService;
import playbook.encore.back.data.dto.bookUser.LoginUserRequestDto;
import playbook.encore.back.data.dto.bookUser.LoginUserResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterIdValidateResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserRequestDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.jwt.jwtUtil;

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
        BookUser user = (BookUser) request.getAttribute("user"); // 인터셉터에서 넣어준 사용자 정보
        return ResponseEntity.ok().body(user);
    }
    @GetMapping("/validate")
    public ResponseEntity<?> getCurrentPassword(
            HttpServletRequest request,
            @RequestBody String password
    ) throws Exception {
        try {
            BookUser user = (BookUser) request.getAttribute("user");
            boolean result = bookUserService.validatePassword(user, password);
            return ResponseEntity.status(HttpStatus.OK).body(result);
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
            BookUser user = (BookUser) request.getAttribute("user");
            boolean result = bookUserService.updatePassword(user, newPassword);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }
    @PutMapping("/discord")
    public ResponseEntity<?> updateDiscord(
            HttpServletRequest request,
            @RequestBody String newDiscord
    ) throws Exception {
        try {
            BookUser user = (BookUser) request.getAttribute("user");
            boolean result = bookUserService.updateDiscord(user, newDiscord);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }
    @PutMapping("/course")
    public ResponseEntity<?> updateCourse(
            HttpServletRequest request,
            @RequestBody Integer newSeqCourse
    ) throws Exception {
        try {
            BookUser user = (BookUser) request.getAttribute("user");
            boolean result = bookUserService.updateCourse(user, newSeqCourse);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }
}
