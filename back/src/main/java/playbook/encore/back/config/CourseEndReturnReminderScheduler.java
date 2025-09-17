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

@Component
@EnableScheduling
public class CourseEndReturnReminderScheduler {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DiscordNotificationService discordNotificationService;

    // 매일 오전 10시에 한 번만 실행하여 모든 날짜 체크
    @Scheduled(cron = "0 0 10 * * ?")
    public void dailyCourseEndCheck() {
        if (!discordNotificationService.isBotOnline()) {
            System.out.println("Discord 봇이 비활성화되어 있어 과정 종료 알림을 건너뜁니다.");
            return;
        }

        LocalDate today = LocalDate.now();

        // 7일 후 종료되는 과정 체크
        checkSpecificDateReminder(today.plusDays(7), 7);

        // 3일 후 종료되는 과정 체크
        checkSpecificDateReminder(today.plusDays(3), 3);

        // 1일 후 종료되는 과정 체크
        checkSpecificDateReminder(today.plusDays(1), 1);
    }

    // 특정 날짜에 종료되는 과정이 있는지 체크하고 알림 전송
    private void checkSpecificDateReminder(LocalDate targetDate, int daysRemaining) {
        try {
            List<Course> endingCourses = courseRepository.findCoursesEndingInDays(targetDate);

            if (endingCourses.isEmpty()) {
                // 종료 예정 과정이 없으면 로그만 남기고 넘어감
                System.out.println("과정 종료 " + daysRemaining + "일 전 체크: 해당 날짜에 종료되는 과정 없음");
                return;
            }

            // 해당 날짜에 종료되는 과정이 있으면 알림 전송
            System.out.println("과정 종료 " + daysRemaining + "일 전 알림: " + endingCourses.size() + "개 과정 발견");

            for (Course course : endingCourses) {
                sendCourseEndNotification(course, daysRemaining);
            }

        } catch (Exception e) {
            System.err.println("과정 종료 " + daysRemaining + "일 전 알림 처리 중 오류 발생: " + e.getMessage());
        }
    }

    // 개별 과정 알림 전송
    private void sendCourseEndNotification(Course course, int daysRemaining) {
        try {
            discordNotificationService.sendCourseEndReturnReminder(
                    course.getNameCourse(),
                    course.getFinishDtCourse().toString(),
                    daysRemaining
            );

            String periodDesc = switch (daysRemaining) {
                case 7 -> "7일 전";
                case 3 -> "3일 전";
                case 1 -> "1일 전";
                default -> daysRemaining + "일 전";
            };

            System.out.println("과정 종료 " + periodDesc + " 알림 전송 완료: " + course.getNameCourse());

        } catch (Exception e) {
            System.err.println("과정 알림 전송 실패 [" + course.getNameCourse() + "]: " + e.getMessage());
        }
    }

    // 수동 테스트용 메서드
//    public void testCourseEndReminder() {
//        System.out.println("=== 과정 종료 알림 테스트 시작 ===");
//        dailyCourseEndCheck();
//        System.out.println("=== 과정 종료 알림 테스트 완료 ===");
//    }
//
//    // 특정 날짜의 과정 종료 알림만 테스트 (개발용)
//    public void testSpecificDayReminder(int daysFromNow) {
//        LocalDate targetDate = LocalDate.now().plusDays(daysFromNow);
//        System.out.println("=== " + daysFromNow + "일 후 과정 종료 알림 테스트 ===");
//        checkSpecificDateReminder(targetDate, daysFromNow);
//        System.out.println("=== 테스트 완료 ===");
//    }
}