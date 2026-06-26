package playbook.encore.back.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 캠퍼스 측 토큰 회전 스케줄러 (AL8 접속이력 파기 스케줄러와 같은 @Scheduled 패턴).
 *
 * <p>액세스 토큰 TTL의 약 50% 주기(기본 3.5일)로 갱신 → 만료 한참 전에 회전해 끊김을 막는다.
 * 갱신은 반드시 캠퍼스가 먼저 요청해야 한다(AWS는 NAT 뒤 캠퍼스로 push 불가).
 * {@code monitoring.token.refresh.enabled=true}일 때만 동작.
 */
@Component
@ConditionalOnProperty(prefix = "monitoring.token.refresh", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class MonitoringTokenScheduler {

    private final MonitoringTokenService tokenService;

    @Scheduled(
            initialDelayString = "${monitoring.token.refresh.initial-delay-ms:60000}",
            fixedDelayString = "${monitoring.token.refresh.interval-ms:302400000}")
    public void scheduledRotate() {
        try {
            tokenService.rotate();
        } catch (Exception e) {
            // 스케줄러 스레드를 죽이지 않도록 방어 — 다음 주기에 재시도
            log.error("[MonitoringToken] 회전 중 예외 — 다음 주기 재시도", e);
        }
    }
}
