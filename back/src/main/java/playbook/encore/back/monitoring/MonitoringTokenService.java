package playbook.encore.back.monitoring;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

/**
 * 캠퍼스 측 모니터링 액세스 토큰 자동 회전.
 *
 * <p>중앙(AWS) {@code POST /auth/token}을 호출해 단기 액세스 토큰을 갱신하고, 결과를
 * {@code bearer_token_file}(Alloy가 remote_write에 사용)에 <b>원자적으로</b> 기록한다.
 *
 * <ol>
 *   <li>현재 토큰(파일 우선, 없으면 초기 ACCESS_TOKEN)으로 grant=refresh 시도</li>
 *   <li>실패(만료·꼬임)하면 BOOTSTRAP_SECRET으로 grant=bootstrap 복구 시도</li>
 *   <li>둘 다 실패하면 기존 토큰 유지(overlap grace 동안 수집은 계속됨) + 에러 로그</li>
 * </ol>
 *
 * {@code monitoring.token.refresh.enabled=true}(캠퍼스 배포)일 때만 빈 생성.
 */
@Service
@ConditionalOnProperty(prefix = "monitoring.token.refresh", name = "enabled", havingValue = "true")
@Slf4j
public class MonitoringTokenService {

    private static final String GRANT_REFRESH = "refresh";
    private static final String GRANT_BOOTSTRAP = "bootstrap";

    private final RestTemplate rest;
    private final String endpoint;
    private final String campus;
    private final String initialAccessToken;
    private final String bootstrapSecret;
    private final Path tokenFile;

    public MonitoringTokenService(
            RestTemplate monitoringRestTemplate,
            @Value("${monitoring.token.endpoint}") String endpoint,
            @Value("${monitoring.token.campus}") String campus,
            @Value("${monitoring.token.access-token}") String initialAccessToken,
            @Value("${monitoring.token.bootstrap-secret}") String bootstrapSecret,
            @Value("${monitoring.token.file}") String tokenFilePath) {
        this.rest = monitoringRestTemplate;
        this.endpoint = endpoint;
        this.campus = campus;
        this.initialAccessToken = initialAccessToken;
        this.bootstrapSecret = bootstrapSecret;
        this.tokenFile = Path.of(tokenFilePath);
    }

    /** 부팅 시 토큰 파일이 없으면 초기 액세스 토큰으로 시드 → Alloy가 첫 갱신 전에도 push 가능. */
    @PostConstruct
    public void seedTokenFileIfAbsent() {
        if (isBlank(campus)) {
            log.error("[MonitoringToken] campus 미설정 — 토큰 회전 비활성과 다름없음. MONITORING_CAMPUS 확인");
            return;
        }
        if (Files.exists(tokenFile)) {
            return;
        }
        if (isBlank(initialAccessToken)) {
            log.warn("[MonitoringToken] 토큰 파일 없음 + 초기 ACCESS_TOKEN 없음 — 첫 갱신 성공 전까지 수집 불가");
            return;
        }
        try {
            writeAtomic(initialAccessToken);
            log.info("[MonitoringToken] 토큰 파일 초기 시드 완료: {}", tokenFile);
        } catch (IOException e) {
            log.error("[MonitoringToken] 토큰 파일 초기 시드 실패: {}", tokenFile, e);
        }
    }

    /** 토큰 1회 회전. 스케줄러가 주기적으로 호출한다. */
    public void rotate() {
        if (isBlank(campus)) {
            log.error("[MonitoringToken] campus 미설정 — 회전 건너뜀");
            return;
        }
        String current = readCurrentToken();

        Optional<String> issued = Optional.empty();
        if (!isBlank(current)) {
            issued = request(GRANT_REFRESH, current);
        }
        if (issued.isEmpty()) {
            log.warn("[MonitoringToken] refresh 실패/불가 — bootstrap 복구 시도 (campus={})", campus);
            issued = request(GRANT_BOOTSTRAP, bootstrapSecret);
        }

        issued.ifPresentOrElse(
                this::persist,
                () -> log.error("[MonitoringToken] refresh·bootstrap 모두 실패 — 기존 토큰 유지(grace 내 복구 필요, campus={})", campus));
    }

    // ── 내부 ──

    private void persist(String newToken) {
        try {
            writeAtomic(newToken);
            log.info("[MonitoringToken] 새 토큰 기록 완료 (campus={}, file={})", campus, tokenFile);
        } catch (IOException e) {
            log.error("[MonitoringToken] 새 토큰 파일 기록 실패 — 기존 토큰 유지 (file={})", tokenFile, e);
        }
    }

    /** 중앙에 grant 요청. 200이고 access_token이 있으면 토큰 반환, 아니면 empty. */
    private Optional<String> request(String grantType, String credential) {
        if (isBlank(credential)) {
            return Optional.empty();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(credential);
            Map<String, String> body = Map.of("campus", campus, "grant_type", grantType);

            ResponseEntity<MonitoringTokenResponse> resp = rest.postForEntity(
                    endpoint, new HttpEntity<>(body, headers), MonitoringTokenResponse.class);

            MonitoringTokenResponse rb = resp.getBody();
            if (resp.getStatusCode().is2xxSuccessful() && rb != null && !isBlank(rb.accessToken())) {
                log.info("[MonitoringToken] {} 성공 (campus={}, expires_at={})", grantType, campus, rb.expiresAt());
                return Optional.of(rb.accessToken());
            }
            log.warn("[MonitoringToken] {} 응답 비정상 (campus={}, status={})", grantType, campus, resp.getStatusCode());
            return Optional.empty();
        } catch (RestClientException e) {
            // 401(자격증명 무효)·네트워크 오류 등 — fallback이 처리
            log.warn("[MonitoringToken] {} 요청 실패 (campus={}): {}", grantType, campus, e.getMessage());
            return Optional.empty();
        }
    }

    /** 파일에 토큰이 있으면 그것을(최신), 없으면 초기 ACCESS_TOKEN을 현재 토큰으로 본다. */
    private String readCurrentToken() {
        try {
            if (Files.exists(tokenFile)) {
                String fromFile = Files.readString(tokenFile, StandardCharsets.UTF_8).trim();
                if (!fromFile.isEmpty()) {
                    return fromFile;
                }
            }
        } catch (IOException e) {
            log.warn("[MonitoringToken] 토큰 파일 읽기 실패, 초기 토큰으로 대체: {}", tokenFile, e);
        }
        return initialAccessToken;
    }

    /** 같은 디렉토리에 임시 파일로 쓴 뒤 원자적으로 교체 — Alloy가 반쪽짜리 토큰을 읽지 않게. */
    private void writeAtomic(String token) throws IOException {
        Path parent = tokenFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmp = Files.createTempFile(parent, ".token", ".tmp");
        try {
            Files.writeString(tmp, token + System.lineSeparator(), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, tokenFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicUnsupported) {
                // 일부 파일시스템은 ATOMIC_MOVE 미지원 → 일반 교체로 폴백
                Files.move(tmp, tokenFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
