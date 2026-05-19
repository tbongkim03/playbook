package playbook.encore.back.discord;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DiscordNotificationService {

    @Value("${DISCORD_CHANNEL_ID:discord-channel-id}")
    private String channelId;

    // 캠퍼스별 채널 ID
    @Value("${DISCORD_CHANNEL_SEOCHO:}")
    private String channelIdSeocho;

    @Value("${DISCORD_CHANNEL_GVALLEY:}")
    private String channelIdGvalley;

    @Value("${DISCORD_CHANNEL_DONGJAK:}")
    private String channelIdDongjak;

    private final JDA jda;

    public DiscordNotificationService(JDA jda) {
        this.jda = jda;
    }

    // 봇 상태 확인 메서드
    private boolean isBotAvailable() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    // 안전한 메시지 전송
    private void sendMessageSafely(Runnable messageAction, String fallbackLog) {
        if (!isBotAvailable()) {
            System.out.println("⚠️ Discord 봇이 비활성화되어 있습니다: " + fallbackLog);
            return;
        }

        if (channelId == null || channelId.isEmpty()) {
            System.out.println("⚠️ Discord 채널 ID가 설정되지 않았습니다: " + fallbackLog);
            return;
        }

        try {
            messageAction.run();
        } catch (Exception e) {
            System.err.println("⚠️ Discord 메시지 전송 실패: " + e.getMessage());
            System.out.println("📝 로그: " + fallbackLog);
        }
    }

    // 기본 채널 메시지
    public void sendMessage(String message) {
        log.info("[DiscordService] 채널 메시지 전송");
        sendMessageSafely(() -> {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }, "채널 메시지: " + message);
    }

    // 개인 DM으로 메시지 보내기
    public void sendDirectMessage(String discordUserId, String userName, String message) {
        log.info("[DiscordService] DM 전송 - userId: {}", discordUserId);
        if (!isBotAvailable()) {
            System.out.println("⚠️ Discord 봇 비활성화: " + userName + "님에게 메시지 전송 실패");
            return;
        }

        try {
            if (isNumericId(discordUserId)) {
                jda.retrieveUserById(discordUserId).queue(
                        user -> {
                            user.openPrivateChannel().queue(
                                    privateChannel -> privateChannel.sendMessage(message).queue()
                            );
                        }
                );
            } else {
                System.out.println("⚠️ Discord ID가 숫자가 아님: " + discordUserId);
            }
        } catch (Exception e) {
            System.err.println("Discord DM 전송 실패: " + e.getMessage());
        }
    }

    // 캠퍼스별 채널 ID 가져오기
    private String getChannelIdByCampus(Integer campusId) {
        if (campusId == null) {
            return channelId; // 기본 채널
        }
        return switch (campusId) {
            case 1 -> channelIdSeocho;
            case 2 -> channelIdGvalley;
            case 3 -> channelIdDongjak;
            default -> channelId;
        };
    }

    // 채널에 메시지 보내기 (기본 채널)
    public void sendChannelMessage(String message) {
        log.info("[DiscordService] 채널 메시지 전송");
        sendMessageSafely(() -> {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }, "메시지: " + " - " + message);
    }

    // 캠퍼스별 채널에 메시지 보내기
    public void sendChannelMessage(String message, Integer campusId) {
        log.info("[DiscordService] 캠퍼스 채널 메시지 전송 - campusId: {}", campusId);
        String targetChannelId = getChannelIdByCampus(campusId);

        if (targetChannelId == null || targetChannelId.isEmpty()) {
            System.out.println("⚠️ 캠퍼스 " + campusId + " 채널 ID가 설정되지 않았습니다.");
            return;
        }

        sendMessageSafely(() -> {
            TextChannel channel = jda.getTextChannelById(targetChannelId);
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }, "캠퍼스 " + campusId + " 메시지: " + message);
    }

    // 대출 완료 알림
    public void sendBorrowNotification(String discordUserId, String userName, String bookTitle, String returnDate) {
        log.info("[DiscordService] 대출 알림 전송 - userId: {}, book: {}", discordUserId, bookTitle);
        String message = String.format("""
            \n📚 **[%s]** 도서 대출이 완료되었습니다.\n
            📅 반납 예정일: %s\n
            💡 반납일을 꼭 지켜주세요!
            """, bookTitle, returnDate);

        sendDirectMessage(discordUserId, userName, message);
    }

    // 반납 완료 알림
    public void sendReturnNotification(String discordUserId, String userName, String bookTitle) {
        log.info("[DiscordService] 반납 알림 전송 - userId: {}, book: {}", discordUserId, bookTitle);
        String message = String.format("""
            \n📚 **[%s]** 도서 반납이 완료되었습니다.\n
            ✅ 반납해 주셔서 감사합니다!
            """, bookTitle);

        sendDirectMessage(discordUserId, userName, message);
    }

    // 반납 예정 알림
    public void sendReturnReminderNotification(String discordUserId, String userName, String bookTitle, String returnDate) {
        log.info("[DiscordService] 반납 예정 알림 전송 - userId: {}, book: {}", discordUserId, bookTitle);
        String message = String.format("""
            \n⏰ **[%s]** 반납 예정일이 내일입니다.\n
            📅 반납일: %s\n
            🏃‍♂️ 연체되지 않도록 미리 준비해 주세요!
            """, bookTitle, returnDate);

        sendDirectMessage(discordUserId, userName, message);
    }

    // 연체 알림
    public void sendOverdueNotification(String discordUserId, String userName, String bookTitle, String returnDate, long overdueDays) {
        log.info("[DiscordService] 연체 알림 전송 - userId: {}, book: {}, days: {}", discordUserId, bookTitle, overdueDays);
        String message = String.format("""
            \n🚨 **[%s]** 도서가 연체되었습니다.\n
            📅 반납 예정일: %s (%d일 경과)\n
            ⛔ 대여 정지 기간: %d일\n
            🏃‍♂️ 즉시 반납해 주세요!
            """, bookTitle, returnDate, overdueDays, overdueDays * 2);

        sendDirectMessage(discordUserId, userName, message);
    }

    // 수강 종료 예정 반 반납 알림
    public void sendCourseEndReturnReminder(String courseName, String endDate, int daysRemaining, Integer campusId) {
        log.info("[DiscordService] 수강 종료 반납 알림 - course: {}, daysRemaining: {}", courseName, daysRemaining);
        String urgencyEmoji = switch (daysRemaining) {
            case 7 -> "📢";
            case 3 -> "⚠️";
            case 1 -> "🚨";
            default -> "📅";
        };

        String urgencyMessage = switch (daysRemaining) {
            case 7 -> "1주일 후";
            case 3 -> "3일 후";
            case 1 -> "내일";
            default -> daysRemaining + "일 후";
        };

        String message = String.format("""
        %s **[%s]** 과정이 %s 종료됩니다!
        📅 종료일: %s

        🏃‍♂️ 과정 종료 전에 미리 반납해 주세요!
        ⚠️ 과정 종료 후에는 도서 반납이 어려울 수 있습니다.
        """, urgencyEmoji, courseName, urgencyMessage, endDate);

        sendChannelMessage(message, campusId);
    }

    // 관심 도서 대출 가능 알림
    public void sendFavorNotification(String dcUser, String nameUser, String titleBook) {
        log.info("[DiscordService] 관심 도서 알림 전송 - userId: {}, book: {}", dcUser, titleBook);
        String message = String.format("""
            \n📚 **[%s]** 도서가 대출 가능 상태가 되었습니다!\n
            📢 관심 도서 알림: %s님께서 찜하신 도서입니다.\n
            🏃‍♂️ 서둘러 대출해 보세요!
            """, titleBook, nameUser);

        sendDirectMessage(dcUser, nameUser, message);
    }

    // Discord ID가 숫자인지 확인
    private boolean isNumericId(String discordId) {
        if (discordId == null || discordId.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(discordId);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 봇 상태 확인 (디버깅용)
    public boolean isBotOnline() {
        return isBotAvailable();
    }
}