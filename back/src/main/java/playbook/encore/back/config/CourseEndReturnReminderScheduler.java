package playbook.encore.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.entity.Course;
import playbook.encore.back.data.repository.CourseRepository;
import playbook.encore.back.service.impl.DiscordNotificationService;

import java.time.LocalDate;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@EnableScheduling
@Slf4j
public class CourseEndReturnReminderScheduler {
    private static final String COURSE_END_NOTIFICATION_FILE = "course_end_notification_last_run.txt";
    
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private DiscordNotificationService discordNotificationService;

    private boolean isNotificationAlreadySentToday() {
        File file = new File(COURSE_END_NOTIFICATION_FILE);
        if (!file.exists()) {
            return false;
        }
        
        try {
            String lastRunDate = Files.readString(file.toPath()).trim();
            return lastRunDate.equals(LocalDate.now().toString());
        } catch (IOException e) {
            log.error("알림 이력 파일 읽기 실패: {}", COURSE_END_NOTIFICATION_FILE, e);
            return false;
        }
    }
    
    private void recordTodayNotification() {
        try {
            Files.writeString(Path.of(COURSE_END_NOTIFICATION_FILE), 
                    LocalDate.now().toString());
        } catch (IOException e) {
            log.error("알림 이력 파일 쓰기 실패: {}", COURSE_END_NOTIFICATION_FILE, e);
        }
    }

    @Scheduled(cron = "0 03 08 * * ?", zone = "Asia/Seoul")
    public void dailyCourseEndCheck() {
        if (!discordNotificationService.isBotOnline()) {
            log.warn("Discord 봇이 비활성화되어 있어 과정 종료 알림을 건너뜁니다.");
            return;
        }

        if (isNotificationAlreadySentToday()) {
            log.info("오늘 이미 과정 종료 알림을 보냈습니다.");
            return;
        }

        LocalDate today = LocalDate.now();

        // 7일 후 종료되는 과정 체크
        checkSpecificDateReminder(today.plusDays(7), 7);

        // 3일 후 종료되는 과정 체크
        checkSpecificDateReminder(today.plusDays(3), 3);

        // 1일 후 종료되는 과정 체크
        checkSpecificDateReminder(today.plusDays(1), 1);

        recordTodayNotification();
        log.info("과정 종료 알림 전송 완료");
    }

    private void checkSpecificDateReminder(LocalDate targetDate, int daysRemaining) {
        try {
            List<Course> endingCourses = courseRepository.findCoursesEndingInDays(targetDate);

            if (endingCourses.isEmpty()) {
                log.debug("과정 종료 {}일 전 체크: 해당 날짜에 종료되는 과정 없음", daysRemaining);
                return;
            }

            log.info("과정 종료 {}일 전 알림: {}개 과정 발견", daysRemaining, endingCourses.size());

            for (Course course : endingCourses) {
                sendCourseEndNotification(course, daysRemaining);
            }

        } catch (Exception e) {
            log.error("과정 종료 {}일 전 알림 처리 중 오류 발생: {}", daysRemaining, e.getMessage());
        }
    }

    private void sendCourseEndNotification(Course course, int daysRemaining) {
        try {
            // 과정의 캠퍼스 ID 가져오기
            Integer campusId = course.getSeqCampus() != null ? course.getSeqCampus().getSeqCampus() : null;

            discordNotificationService.sendCourseEndReturnReminder(
                    course.getNameCourse(),
                    course.getFinishDtCourse().toString(),
                    daysRemaining,
                    campusId  // 캠퍼스 ID 전달
            );

            String periodDesc = switch (daysRemaining) {
                case 7 -> "7일 전";
                case 3 -> "3일 전";
                case 1 -> "1일 전";
                default -> daysRemaining + "일 전";
            };

            String campusName = course.getSeqCampus() != null ? course.getSeqCampus().getNameCampus() : "미지정";
            log.info("과정 종료 {} 알림 전송 완료: {} (캠퍼스: {})", periodDesc, course.getNameCourse(), campusName);

        } catch (Exception e) {
            log.error("과정 알림 전송 실패 [{}]: {}", course.getNameCourse(), e.getMessage());
        }
    }
}