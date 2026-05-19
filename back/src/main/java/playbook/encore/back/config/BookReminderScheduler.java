package playbook.encore.back.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import playbook.encore.back.data.entity.Campus;
import playbook.encore.back.data.entity.Course;
import playbook.encore.back.data.entity.History;
import playbook.encore.back.data.repository.AdminRepository;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.data.repository.CampusRepository;
import playbook.encore.back.data.repository.CourseRepository;
import playbook.encore.back.data.repository.HistoryRepository;
import playbook.encore.back.service.impl.DiscordNotificationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@Slf4j
public class BookReminderScheduler {
    private static final String RETURN_NOTIFICATION_FILE = "return_notification_last_run.txt";
    private static final String OVERDUE_NOTIFICATION_FILE = "overdue_notification_last_run.txt";
    private static final String STATUS_UPDATE_FILE = "status_update_last_run.txt";

    @Autowired
    private PlatformTransactionManager transactionManager;

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
    @Autowired
    private CampusRepository campusRepository;

    private boolean isNotificationAlreadySentToday(String notificationFile) {
        File file = new File(notificationFile);
        if (!file.exists()) {
            return false;
        }

        try {
            String lastRunDate = Files.readString(file.toPath()).trim();
            return lastRunDate.equals(LocalDate.now().toString());
        } catch (IOException e) {
            log.error("알림 이력 파일 읽기 실패: {}", notificationFile, e);
            return false;
        }
    }

    private void recordTodayNotification(String notificationFile) {
        try {
            Files.writeString(Path.of(notificationFile),
                    LocalDate.now().toString());
        } catch (IOException e) {
            log.error("알림 이력 파일 쓰기 실패: {}", notificationFile, e);
        }
    }


    // 서버 시작 시 상태 업데이트 실행
    @PostConstruct
    public void initializeUserStatuses() {
        // 이력 체크 없이 무조건 실행
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            try {
                updateStatusForFinishedCourses();
                log.info("서버 시작 시 과정 종료된 학생들의 상태 업데이트 완료");
            } catch (Exception e) {
                log.error("서버 시작 시 상태 업데이트 중 오류 발생: {}", e.getMessage());
                status.setRollbackOnly();
            }
            return null;
        });

    }

    // 매일 오전 8시에 반납 알림
    @Scheduled(cron = "0 0 08 * * ?", zone = "Asia/Seoul")
    public void sendReturnReminder() {
        if (isNotificationAlreadySentToday(RETURN_NOTIFICATION_FILE)) {
            log.info("오늘 이미 반납 알림을 보냈습니다.");
            return;
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // 모든 활성 캠퍼스에 대해 반복
        List<Campus> activeCampuses = campusRepository.findByIsActiveTrue();

        int totalSent = 0;
        for (Campus campus : activeCampuses) {
            log.info("캠퍼스 '{}' 반납 알림 처리 시작", campus.getNameCampus());

            // 캠퍼스별 내일이 반납일인 도서들 조회
            List<History> dueTomorrow = historyRepository
                    .findByBookDtAndReturnDtIsNullAndSeqCampus_SeqCampus(
                            tomorrow.minusDays(7),
                            campus.getSeqCampus()
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

            totalSent += dueTomorrow.size();
            log.info("캠퍼스 '{}' 반납 알림 완료: {}건", campus.getNameCampus(), dueTomorrow.size());
        }

        recordTodayNotification(RETURN_NOTIFICATION_FILE);
        log.info("전체 반납 알림 발송 완료: 총 {}건", totalSent);
    }

    // 매일 오전 10시 1분에 연체 알림
    @Scheduled(cron = "0 01 08 * * ?", zone = "Asia/Seoul")
    public void sendOverdueNotification() {
        if (isNotificationAlreadySentToday(OVERDUE_NOTIFICATION_FILE)) {
            log.info("오늘 이미 연체 알림을 보냈습니다.");
            return;
        }

        LocalDate today = LocalDate.now();

        // 모든 활성 캠퍼스에 대해 반복
        List<Campus> activeCampuses = campusRepository.findByIsActiveTrue();

        int totalSent = 0;
        for (Campus campus : activeCampuses) {
            log.info("캠퍼스 '{}' 연체 알림 처리 시작", campus.getNameCampus());

            // 캠퍼스별 연체된 도서들 조회
            List<History> overdueBooks = historyRepository.findOverdueBooksByCampus(
                    today.minusDays(7),
                    campus.getSeqCampus()
            );

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

            totalSent += overdueBooks.size();
            log.info("캠퍼스 '{}' 연체 알림 완료: {}건", campus.getNameCampus(), overdueBooks.size());
        }

        recordTodayNotification(OVERDUE_NOTIFICATION_FILE);
        log.info("전체 연체 알림 발송 완료: 총 {}건", totalSent);
    }

    // 과정 종료된 학생들의 상태 업데이트 (08:02 실행)
    @Scheduled(cron = "0 02 08 * * ?", zone = "Asia/Seoul")
    @Transactional
    public void updateStatusForFinishedCourses() {
        LocalDate today = LocalDate.now();
        LocalDate overdueDate = today.minusDays(7);

        try {
            // 기존 로직 유지
            List<Course> finishedCourses = courseRepository.findCoursesFinishedBefore(today);

            for (Course course : finishedCourses) {
                bookUserRepository.updateStatusByCourse(course, overdueDate);
            }

            bookUserRepository.updateStatusForNonExistentCourses(overdueDate);

            if (!finishedCourses.isEmpty()) {
                log.info("과정 종료로 인한 학생 상태 업데이트 완료: {}개 과정", finishedCourses.size());
            }
        } catch (Exception e) {
            log.error("과정 종료된 학생들의 상태 업데이트 중 오류 발생: {}", e.getMessage());
            throw e;
        }

    }
}