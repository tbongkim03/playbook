package playbook.encore.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.entity.Course;
import playbook.encore.back.data.entity.History;
import playbook.encore.back.data.repository.AdminRepository;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.data.repository.CourseRepository;
import playbook.encore.back.data.repository.HistoryRepository;
import playbook.encore.back.service.impl.DiscordNotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class BookReminderScheduler {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private DiscordNotificationService discordNotificationService;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BookUserRepository bookUserRepository;
    @Autowired
    private CourseRepository courseRepository;

    // 매일 오전 10시에 알림 (한국 시간 기준)
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Seoul")
    public void sendReturnReminder() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // 내일이 반납일인 도서들 조회
        List<History> dueTomorrow = historyRepository.findByBookDtAndReturnDtIsNull(
                tomorrow.minusDays(7)
        );

        for (History history : dueTomorrow) {
            String discordId = null;
            String userName = null;

            if (history.getSeqUser() != null) {
                discordId = history.getSeqUser().getDcUser();
                userName = history.getSeqUser().getNameUser();
            } else if (history.getSeqAdmin() != null) {
                discordId = history.getSeqAdmin().getDcAdmin();
                userName = history.getSeqAdmin().getNameAdmin();
            }

            if (discordId != null) {
                discordNotificationService.sendReturnReminderNotification(
                        discordId,
                        userName,
                        history.getSeqBook().getTitleBook(),
                        tomorrow.toString()
                );
            }
        }
    }

    // 매일 오전 10시에 연체 알림 (한국 시간 기준)
    @Scheduled(cron = "0 01 10 * * ?", zone = "Asia/Seoul")
    public void sendOverdueNotification() {
        LocalDate today = LocalDate.now();

        // 연체된 도서들 조회
        List<History> overdueBooks = historyRepository.findOverdueBooks(today.minusDays(7));

        for (History history : overdueBooks) {
            String discordId = null;
            String userName = null;

            if (history.getSeqUser() != null) {
                discordId = history.getSeqUser().getDcUser();
                userName = history.getSeqUser().getNameUser();
            } else if (history.getSeqAdmin() != null) {
                discordId = history.getSeqAdmin().getDcAdmin();
                userName = history.getSeqAdmin().getNameAdmin();
            }

            if (discordId != null) {
                long overdueDays = today.toEpochDay() - history.getBookDt().plusDays(7).toEpochDay();
                discordNotificationService.sendOverdueNotification(
                        discordId,
                        userName,
                        history.getSeqBook().getTitleBook(),
                        history.getBookDt().plusDays(7).toString(),
                        overdueDays
                );
            }
        }
    }

    // 매일 오전 7시에 유저, 어드민 상태 업데이트 (한국 시간 기준)
    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Seoul")
    @Transactional
    public void updateUserStatus() {
        LocalDate overdueDate = LocalDate.now().minusDays(7);
        adminRepository.updateAllAdminStatus(overdueDate);
        bookUserRepository.updateAllStatusUser(overdueDate);
        System.out.println("사용자 상태 업데이트 완료: " + LocalDateTime.now());
    }

    // 매일 오전 8시에 과정 종료된 학생들의 상태를 stop으로 변경 (연체 중인 학생 제외)
    // 테스트용: 오후 9시 30분에 실행 (한국 시간 기준)
    @Scheduled(cron = "0 40 21 * * ?", zone = "Asia/Seoul")
    @Transactional
    public void updateStatusForFinishedCourses() {
        LocalDate today = LocalDate.now();
        LocalDate overdueDate = today.minusDays(7);
        
        // finishDtCourse가 현재 날짜보다 지난 Course 조회
        List<Course> finishedCourses = courseRepository.findCoursesFinishedBefore(today);
        
        for (Course course : finishedCourses) {
            // 해당 Course를 수강하는 학생들의 상태를 stop으로 변경 (연체 중인 학생 제외)
            bookUserRepository.updateStatusByCourse(course, overdueDate);
        }
        
        // Course 테이블에 존재하지 않는 과정을 참조하는 학생들의 상태도 stop으로 변경 (연체 중인 학생 제외)
        bookUserRepository.updateStatusForNonExistentCourses(overdueDate);
        
        if (!finishedCourses.isEmpty()) {
            System.out.println("과정 종료로 인한 학생 상태 업데이트 완료: " + finishedCourses.size() + "개 과정, " + LocalDateTime.now());
        }
    }
}