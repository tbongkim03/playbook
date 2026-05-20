package playbook.encore.back.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.admin.dto.*;
import playbook.encore.back.bookUser.dto.RegisterIdValidateResponseDto;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseCode;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.admin.service.AdminService;



@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final RedisIndexedSessionRepository sessionRepository;

    @Autowired
    public AdminController(AdminService adminService,
                           RedisIndexedSessionRepository sessionRepository) {
        this.adminService = adminService;
        this.sessionRepository = sessionRepository;
    }

    // 회원가입 관련 부분
    @PostMapping("/register")
    public ResponseEntity<Response> registerAdmin(
            HttpServletRequest request,
            @RequestBody @Valid RegisterAdminRequestDto registerAdminRequestDto
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            Admin user = (Admin) request.getAttribute("admin");
            RegisterAdminResponseDto registerAdminResponseDto = adminService.createAdmin(user, registerAdminRequestDto);
            return ResponseEntity.ok(ResponseHandler.success(registerAdminResponseDto));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
    }

    @GetMapping("/register/validate")
    public ResponseEntity<Response> validationId(@RequestParam("id") String idAdmin) throws Exception {
        RegisterIdValidateResponseDto registerIdValidateResponseDto = adminService.checkUserId(idAdmin);
        return ResponseEntity.ok(ResponseHandler.success(registerIdValidateResponseDto));
    }

    // 로그인 관련 부분
    @PostMapping("/login")
    public ResponseEntity<Response> loginUser(
            HttpServletRequest request,
            @RequestBody @Valid LoginAdminRequestDto loginAdminRequestDto) throws Exception {
        try {
            String adminId = adminService.loginServiceAdmin(loginAdminRequestDto);

            // 기존 세션 만료 (중복 로그인 방지)
            sessionRepository.findByIndexNameAndIndexValue(
                    FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, adminId)
                    .keySet().forEach(sessionRepository::deleteById);

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", adminId);
            session.setAttribute("role", "admin");
            session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, adminId);
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
    public ResponseEntity<Response> logoutAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ResponseHandler.success());
    }

    // 회원정보 관련 부분
    @GetMapping("/me")
    public ResponseEntity<Response> getAdminInfo(HttpServletRequest request) {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            Admin user = (Admin) request.getAttribute("admin");
            LoginAdminDataResponseDto loginAdminDataResponseDto = new LoginAdminDataResponseDto(
                user.getSeqCampus(),
                user.getIdAdmin(),
                user.getNameAdmin(),
                user.getDcAdmin()
            );
            return ResponseEntity.ok(ResponseHandler.success(loginAdminDataResponseDto));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
    }

    @PostMapping("/validate")
    public ResponseEntity<Response> getCurrentPassword(
            HttpServletRequest request,
            @RequestParam("id") String idAdmin,
            @RequestBody @Valid AdminPasswordValidateRequestDto dto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                boolean result = adminService.validatePassword(user, idAdmin, dto.getPassword());
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
            @RequestBody @Valid AdminPasswordUpdateRequestDto dto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                boolean result = adminService.updatePassword(user, dto.getNewPassword());
                return ResponseEntity.ok(ResponseHandler.success(result));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @PutMapping("/discord")
    public ResponseEntity<Response> updateDiscord(
            HttpServletRequest request,
            @RequestBody AdminDiscordUpdateRequestDto dto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                boolean result = adminService.updateDiscord(user, dto.getNewDiscord());
                return ResponseEntity.ok(ResponseHandler.success(result));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Response> updateAdmin(
            HttpServletRequest request,
            @RequestBody @Valid UpdateAdminRequestDto updateRequest
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                boolean result = adminService.updateAdmin(user, updateRequest);
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
    public ResponseEntity<Response> getAdminList(
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
                AdminListResponseDto adminListResponseDto = adminService.getAdminList(campusId);
                return ResponseEntity.ok(ResponseHandler.success(adminListResponseDto));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @DeleteMapping
    public ResponseEntity<Response> deleteAdmin(
            HttpServletRequest request,
            @RequestBody @Valid AdminDeleteRequestDto dto
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                boolean result = adminService.deleteAdmin(dto.getIdAdmin());
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
