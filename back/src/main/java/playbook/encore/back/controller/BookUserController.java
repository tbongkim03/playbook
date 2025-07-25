package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import playbook.encore.back.data.dao.BookUserDAO;
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
    private final BookUserDAO bookUserDAO;
    private final BookUserRepository bookUserRepository;
    private final jwtUtil jwtUtil;

    @Autowired
    public BookUserController(BookUserService bookUserService, BookUserDAO bookUserDAO, BookUserRepository bookUserRepository, jwtUtil jwtUtil) {
        this.bookUserService = bookUserService;
        this.bookUserDAO = bookUserDAO;
        this.bookUserRepository = bookUserRepository;
        this.jwtUtil = jwtUtil;
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

    @GetMapping("/login/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String result = jwtUtil.validateAndGetReason(token);

            switch (result) {
                case "VALID":
                    String userId = jwtUtil.getIdUserFromToken(token);
                    BookUser user = bookUserRepository.findByIdUser(userId)
                            .orElseThrow(() -> new RuntimeException("사용자 정보 없음"));
                    return ResponseEntity.ok().body(user.getNameUser() + "님 환영합니다!");
                case "EXPIRED":
                    return ResponseEntity.status(401).body("토큰이 만료되었습니다.");
                case "INVALID":
                default:
                    return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
            }
        }

        return ResponseEntity.status(401).body("인증 토큰이 없습니다.");
    }


    // 회원정보 관련 부분

}
