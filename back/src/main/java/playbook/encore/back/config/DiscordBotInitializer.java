package playbook.encore.back.config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playbook.encore.back.listener.PlaybookListener;

@Configuration
public class DiscordBotInitializer {

    @Value("${DISCORD_BOT_TOKEN:discord-bot-token}")
    private String token;

    @Autowired
    private PlaybookListener playbookListener;

    @Bean
    public JDA jda() throws Exception {
        // 토큰이 비어있거나 기본값인 경우 처리
        if (token == null || token.isEmpty() || "discord-bot-token".equals(token)) {
            System.err.println("❌ Discord 봇 토큰이 설정되지 않았습니다.");
            System.err.println("💡 application.properties에 DISCORD_BOT_TOKEN을 설정해주세요.");
            return null; // null 반환으로 봇 비활성화
        }

        try {
            System.out.println("🤖 Discord 봇 초기화 중...");
            JDA jda = JDABuilder.createDefault(token)
                    .setActivity(Activity.playing("플북 📚 도서 관리"))
                    .addEventListeners(playbookListener)
                    .build();

            System.out.println("⏳ Discord 봇 연결 대기 중...");
            jda.awaitReady();
            System.out.println("✅ Discord 봇이 성공적으로 시작되었습니다.");
            return jda;

        } catch (Exception e) {
            System.err.println("❌ Discord 봇 초기화 실패: " + e.getMessage());
            System.err.println("💡 토큰과 권한을 확인해주세요.");
            return null; // 실패 시 null 반환
        }
    }
}