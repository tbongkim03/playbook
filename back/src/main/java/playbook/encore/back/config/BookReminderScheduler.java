package playbook.encore.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.entity.History;
import playbook.encore.back.data.repository.HistoryRepository;
import playbook.encore.back.service.impl.DiscordNotificationService;

import java.time.LocalDate;
import java.util.List;

@Component
@EnableScheduling
public class BookReminderScheduler {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private DiscordNotificationService discordNotificationService;

    // 매일 오전 10시에 알림
    @Scheduled(cron = "0 0 10 * * ?")
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

    // 매일 오전 10시에 연체 알림
    @Scheduled(cron = "0 0 10 * * ?")
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
}