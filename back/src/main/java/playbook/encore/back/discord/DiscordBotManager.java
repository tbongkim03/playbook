package playbook.encore.back.discord;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Component;
import playbook.encore.back.integration.service.IntegrationService;
import playbook.encore.back.listener.PlaybookListener;

/**
 * 디스코드 봇 연결 수명주기 관리.
 * 토큰은 .env 가 아닌 DB(IntegrationService)에서 읽으며, 운영자가 토큰을 변경하면
 * reconnect() 로 JDA 를 재빌드한다.
 */
@Slf4j
@Component
public class DiscordBotManager {

    private final IntegrationService integrationService;
    private final PlaybookListener playbookListener;

    private volatile JDA jda;

    public DiscordBotManager(IntegrationService integrationService, PlaybookListener playbookListener) {
        this.integrationService = integrationService;
        this.playbookListener = playbookListener;
    }

    @PostConstruct
    public void init() {
        connect();
    }

    /** 현재 JDA 인스턴스 (null 이면 미연결) */
    public JDA getJda() {
        return jda;
    }

    public boolean isConnected() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    /** DB 토큰으로 봇 연결. 토큰 미설정/실패 시 jda = null (봇 비활성). */
    public synchronized void connect() {
        String token = integrationService.getDiscordBotToken();
        if (token == null || token.isBlank() || "discord-bot-token".equals(token)) {
            log.warn("[DiscordBot] 봇 토큰이 설정되지 않았습니다. 연동 관리 탭에서 토큰을 등록해주세요.");
            this.jda = null;
            return;
        }
        try {
            log.info("[DiscordBot] 초기화 중...");
            JDA built = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                    .setActivity(Activity.playing("플북 📚 도서 관리"))
                    .addEventListeners(playbookListener)
                    .build();
            built.awaitReady();
            this.jda = built;
            log.info("[DiscordBot] 연결 성공");
        } catch (Exception e) {
            log.error("[DiscordBot] 초기화 실패: {}", e.getMessage());
            this.jda = null;
        }
    }

    /** 기존 연결을 종료하고 DB 토큰으로 재연결 (운영자 토큰 변경 시 호출) */
    public synchronized void reconnect() {
        if (jda != null) {
            try {
                jda.shutdownNow();
            } catch (Exception e) {
                log.warn("[DiscordBot] 기존 연결 종료 중 오류: {}", e.getMessage());
            }
            this.jda = null;
        }
        connect();
    }

    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            jda.shutdownNow();
        }
    }
}
