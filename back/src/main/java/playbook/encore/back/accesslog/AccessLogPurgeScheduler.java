package playbook.encore.back.accesslog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.accesslog.dao.AccessLogRepository;

import java.time.LocalDateTime;

/**
 * 약관 이행 — 접속이력 1년 자동 파기 스케줄러.
 * 매일 새벽 2시에 실행되며, 1년 이상 경과한 접속이력을 삭제한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessLogPurgeScheduler {

    private final AccessLogRepository accessLogRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldAccessLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(1);
        int deleted = accessLogRepository.deleteOlderThan(cutoff);
        log.info("[AccessLogPurge] 1년 경과 접속이력 {}건 삭제 (기준: {})", deleted, cutoff.toLocalDate());
    }
}
