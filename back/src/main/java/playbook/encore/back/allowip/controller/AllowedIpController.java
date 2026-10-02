package playbook.encore.back.allowip.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.allowip.dto.AllowedIpActiveUpdateRequestDto;
import playbook.encore.back.allowip.dto.AllowedIpRequestDto;
import playbook.encore.back.allowip.exception.AllowedIpException;
import playbook.encore.back.allowip.filter.ClientIpResolver;
import playbook.encore.back.allowip.service.AllowedIpActor;
import playbook.encore.back.allowip.service.AllowedIpService;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.common.util.AuthUtil;

/**
 * 허용 IP 관리 (관리자 전용).
 *
 * <p>권한 스코프
 * <ul>
 *   <li>전체관리자(seqCampus == null): 전 캠퍼스 + 전역 규칙 전부 조회·수정</li>
 *   <li>캠퍼스 관리자: 자기 캠퍼스 규칙만 조회·생성·수정·삭제. 전역 규칙은 조회만</li>
 *   <li>일반 사용자: 전 경로 3002</li>
 * </ul>
 */
@RestController
@RequestMapping("/allowed-ips")
@RequiredArgsConstructor
public class AllowedIpController {

    private final AllowedIpService allowedIpService;
    private final ClientIpResolver clientIpResolver;

    // ===== 1. 목록 =====
    @GetMapping
    public ResponseEntity<Response> getAllowedIps(
            HttpServletRequest request,
            @RequestParam(value = "seqCampus", required = false) Integer seqCampus,
            @RequestParam(value = "includeInactive", required = false, defaultValue = "true") boolean includeInactive) {
        AuthUtil.requireAdmin(request);
        try {
            return ResponseEntity.ok(ResponseHandler.success(allowedIpService.getAllowedIps(
                    actorOf(request),
                    seqCampus,
                    includeInactive,
                    clientIpResolver.resolve(request))));
        } catch (AllowedIpException e) {
            return toError(e);
        }
    }

    // ===== 2. 등록 =====
    @PostMapping
    public ResponseEntity<Response> createAllowedIp(
            HttpServletRequest request,
            @Valid @RequestBody AllowedIpRequestDto body) {
        AuthUtil.requireAdmin(request);
        try {
            return ResponseEntity.ok(ResponseHandler.success(allowedIpService.createAllowedIp(
                    actorOf(request),
                    body,
                    clientIpResolver.resolve(request))));
        } catch (AllowedIpException e) {
            return toError(e);
        }
    }

    // ===== 3. 수정 =====
    @PutMapping("/{seqAllowedIp}")
    public ResponseEntity<Response> updateAllowedIp(
            HttpServletRequest request,
            @PathVariable("seqAllowedIp") Integer seqAllowedIp,
            @Valid @RequestBody AllowedIpRequestDto body) {
        AuthUtil.requireAdmin(request);
        try {
            return ResponseEntity.ok(ResponseHandler.success(allowedIpService.updateAllowedIp(
                    actorOf(request),
                    seqAllowedIp,
                    body,
                    clientIpResolver.resolve(request))));
        } catch (AllowedIpException e) {
            return toError(e);
        }
    }

    // ===== 4. 삭제 (Soft Delete) =====
    @DeleteMapping("/{seqAllowedIp}")
    public ResponseEntity<Response> deleteAllowedIp(
            HttpServletRequest request,
            @PathVariable("seqAllowedIp") Integer seqAllowedIp) {
        AuthUtil.requireAdmin(request);
        try {
            return ResponseEntity.ok(ResponseHandler.success(allowedIpService.deleteAllowedIp(
                    actorOf(request),
                    seqAllowedIp,
                    clientIpResolver.resolve(request))));
        } catch (AllowedIpException e) {
            return toError(e);
        }
    }

    // ===== 5. 활성/비활성 토글 =====
    @PatchMapping("/{seqAllowedIp}/active")
    public ResponseEntity<Response> toggleActive(
            HttpServletRequest request,
            @PathVariable("seqAllowedIp") Integer seqAllowedIp,
            @Valid @RequestBody AllowedIpActiveUpdateRequestDto body) {
        AuthUtil.requireAdmin(request);
        try {
            return ResponseEntity.ok(ResponseHandler.success(allowedIpService.toggleActive(
                    actorOf(request),
                    seqAllowedIp,
                    body.getIsActive(),
                    clientIpResolver.resolve(request))));
        } catch (AllowedIpException e) {
            return toError(e);
        }
    }

    // ===== 6. 내 IP 확인 =====
    @GetMapping("/my-ip")
    public ResponseEntity<Response> getMyIp(HttpServletRequest request) {
        AuthUtil.requireAdmin(request);
        return ResponseEntity.ok(ResponseHandler.success(
                allowedIpService.getMyIp(clientIpResolver.resolve(request))));
    }

    /**
     * 요청자의 권한 스코프를 확정한다.
     *
     * <p>{@code getCampusId} 의 두 번째 인자를 항상 {@code null} 로 둔다 —
     * 요청 파라미터로 캠퍼스를 갈아끼우는 IDOR 경로를 막기 위해 세션 값만 쓴다.
     */
    private AllowedIpActor actorOf(HttpServletRequest request) {
        return new AllowedIpActor(
                AuthUtil.isSuperAdmin(request),
                AuthUtil.getCampusId(request, null));
    }

    /** 도메인 예외를 계약서 코드 그대로 통일 응답으로 변환한다. */
    private ResponseEntity<Response> toError(AllowedIpException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(ResponseHandler.error(e.getResponseCode(), e.getMessage()));
    }
}
