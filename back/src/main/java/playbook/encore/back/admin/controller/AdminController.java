package playbook.encore.back.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.admin.dto.*;
import playbook.encore.back.bookUser.dto.RegisterIdValidateResponseDto;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseCode;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.admin.service.AdminService;
import playbook.encore.back.discord.DiscordNotificationService;

import playbook.encore.back.common.excel.ExcelUtil;
import playbook.encore.back.common.util.AuthUtil;
import playbook.encore.back.common.util.SessionUtil;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final RedisIndexedSessionRepository sessionRepository;
    private final DiscordNotificationService discordNotificationService;

    @Autowired
    public AdminController(AdminService adminService,
                           RedisIndexedSessionRepository sessionRepository,
                           DiscordNotificationService discordNotificationService) {
        this.adminService = adminService;
        this.sessionRepository = sessionRepository;
        this.discordNotificationService = discordNotificationService;
    }

    // 회원가입 관련 부분
    @PostMapping("/register")
    public ResponseEntity<Response> registerAdmin(
            HttpServletRequest request,
            @RequestBody @Valid RegisterAdminRequestDto registerAdminRequestDto
    ) throws Exception {
        Admin user = AuthUtil.getAdmin(request);
        RegisterAdminResponseDto registerAdminResponseDto = adminService.createAdmin(user, registerAdminRequestDto);
        return ResponseEntity.ok(ResponseHandler.success(registerAdminResponseDto));
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
            SessionUtil.createSession(request, sessionRepository, adminId, "admin");
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
        Admin user = AuthUtil.getAdmin(request);
        LoginAdminDataResponseDto loginAdminDataResponseDto = new LoginAdminDataResponseDto(
            user.getSeqCampus(),
            user.getIdAdmin(),
            user.getNameAdmin(),
            user.getDcAdmin()
        );
        return ResponseEntity.ok(ResponseHandler.success(loginAdminDataResponseDto));
    }

    @PostMapping("/validate")
    public ResponseEntity<Response> getCurrentPassword(
            HttpServletRequest request,
            @RequestParam("id") String idAdmin,
            @RequestBody @Valid AdminPasswordValidateRequestDto dto
    ) throws Exception {
        Admin user = AuthUtil.getAdmin(request);
        boolean result = adminService.validatePassword(user, idAdmin, dto.getPassword());
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PutMapping("/password")
    public ResponseEntity<Response> updatePassword(
            HttpServletRequest request,
            @RequestBody @Valid AdminPasswordUpdateRequestDto dto
    ) throws Exception {
        Admin user = AuthUtil.getAdmin(request);
        boolean result = adminService.updatePassword(user, dto.getNewPassword());
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PutMapping("/discord")
    public ResponseEntity<Response> updateDiscord(
            HttpServletRequest request,
            @RequestBody AdminDiscordUpdateRequestDto dto
    ) throws Exception {
        Admin user = AuthUtil.getAdmin(request);
        boolean result = adminService.updateDiscord(user, dto.getNewDiscord());
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PutMapping("/update")
    public ResponseEntity<Response> updateAdmin(
            HttpServletRequest request,
            @RequestBody @Valid UpdateAdminRequestDto updateRequest
    ) throws Exception {
        Admin user = AuthUtil.getAdmin(request);
        boolean result = adminService.updateAdmin(user, updateRequest);
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @GetMapping("/list")
    public ResponseEntity<Response> getAdminList(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        Integer campusId = AuthUtil.getCampusId(request, requestCampusId);
        AdminListResponseDto adminListResponseDto = adminService.getAdminList(campusId);
        return ResponseEntity.ok(ResponseHandler.success(adminListResponseDto));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        Integer campusId = AuthUtil.getCampusId(request, requestCampusId);
        byte[] data = adminService.exportExcel(campusId);
        return ExcelUtil.toResponse(data, "관리자계정");
    }

    @DeleteMapping
    public ResponseEntity<Response> deleteAdmin(
            HttpServletRequest request,
            @RequestBody @Valid AdminDeleteRequestDto dto
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        boolean result = adminService.deleteAdmin(dto.getIdAdmin());
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PostMapping("/discord/link-message")
    public ResponseEntity<Response> postDiscordLinkMessage(
            HttpServletRequest request,
            @RequestParam("channelId") String channelId
    ) {
        AuthUtil.requireAdmin(request);
        discordNotificationService.sendLinkButtonMessage(channelId);
        return ResponseEntity.ok(ResponseHandler.success());
    }
}
