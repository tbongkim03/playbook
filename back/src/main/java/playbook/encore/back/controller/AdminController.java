package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dto.admin.*;
import playbook.encore.back.data.dto.bookUser.*;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.AdminService;

import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 회원가입 관련 부분
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(
            HttpServletRequest request,
            @RequestBody RegisterAdminRequestDto registerAdminRequestDto
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            Admin user = (Admin) request.getAttribute("admin");
            RegisterAdminResponseDto registerAdminResponseDto = adminService.createAdmin(user, registerAdminRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(registerAdminResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @GetMapping("/register/validate")
    public ResponseEntity<RegisterIdValidateResponseDto> validationId(@RequestParam("id") String idAdmin) throws Exception {
        RegisterIdValidateResponseDto registerIdValidateResponseDto = adminService.checkUserId(idAdmin);
        return ResponseEntity.status(HttpStatus.OK).body(registerIdValidateResponseDto);
    }

    // 로그인 관련 부분
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginAdminRequestDto loginAdminRequestDto) throws Exception {
        try {
            LoginAdminResponseDto loginAdminResponseDto = adminService.loginServiceAdmin(loginAdminRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(loginAdminResponseDto);
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }

    // 회원정보 관련 부분
    @GetMapping("/me")
    public ResponseEntity<?> getAdminInfo(HttpServletRequest request) {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            Admin user = (Admin) request.getAttribute("admin");
            LoginAdminDataResponseDto loginAdminDataResponseDto = new LoginAdminDataResponseDto(user.getIdAdmin(), user.getNameAdmin(), user.getDcAdmin());
            return ResponseEntity.status(HttpStatus.OK).body(loginAdminDataResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }
    @PostMapping("/validate")
    public ResponseEntity<?> getCurrentPassword(
            HttpServletRequest request,
            @RequestParam("id") String idAdmin,
            @RequestBody String password
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                boolean result = adminService.validatePassword(user, idAdmin, password);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
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
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                boolean result = adminService.updatePassword(user, newPassword);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
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
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                boolean result = adminService.updateDiscord(user, newDiscord);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAdminList(
            HttpServletRequest request
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                AdminListResponseDto adminListResponseDto = adminService.getAdminList();
                return ResponseEntity.status(HttpStatus.OK).body(adminListResponseDto);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAdmin(
            HttpServletRequest request,
            @RequestBody String idAdmin
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                boolean result = adminService.deleteAdmin(idAdmin);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }
}
