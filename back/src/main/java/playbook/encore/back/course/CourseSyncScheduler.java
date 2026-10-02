package playbook.encore.back.course;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import playbook.encore.back.course.service.CourseSyncService;

/**
 * Work24 훈련과정 동기화 스케줄러.
 * 기존 02:00(접속이력)·08:00~08:03(도서/과정 알림) 슬롯과 겹치지 않도록 매일 09:00 KST 실행.
 * 회원가입 페이지(PageRegister.vue)에서 매 로드마다 수행하던 외부 API 동기화를 대체한다.
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CourseSyncScheduler {

    private final CourseSyncService courseSyncService;

    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Seoul")
    public void dailyCourseSync() {
        log.info("[CourseSyncScheduler] 일일 과정 동기화 시작");
        try {
            CourseSyncService.SyncResult result = courseSyncService.sync();
            log.info("[CourseSyncScheduler] 완료 - 추가 {}, 수정 {}, 삭제 {}",
                    result.inserted(), result.updated(), result.deleted());
        } catch (Exception e) {
            log.error("[CourseSyncScheduler] 과정 동기화 실패: {}", e.getMessage());
        }
    }
}
