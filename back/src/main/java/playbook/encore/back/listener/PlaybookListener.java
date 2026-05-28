package playbook.encore.back.listener;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import playbook.encore.back.admin.dao.AdminDAO;
import playbook.encore.back.bookUser.dao.BookUserDAO;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.bookUser.dao.BookUserRepository;

@Component
public class PlaybookListener extends ListenerAdapter {
    @Autowired
    private BookUserRepository bookUserRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BookUserDAO bookUserDAO;

    @Autowired
    private AdminDAO adminDAO;

    private static final String LINK_BUTTON_ID = "playbook_discord_link";

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String discordUsername = event.getUser().getName();
        String discordUserId = event.getUser().getId();

        bookUserRepository.findByDcUser(discordUsername).ifPresent(user -> {
            bookUserDAO.changeDiscord(user, discordUserId);
        });

        adminRepository.findByDcAdmin(discordUsername).ifPresent(admin -> {
            adminDAO.changeDiscord(admin, discordUserId);
        });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!LINK_BUTTON_ID.equals(event.getComponentId())) return;

        String discordUsername = event.getUser().getName();
        String discordUserId = event.getUser().getId();

        // 이미 연동된 경우 (dc_user가 이미 Snowflake ID로 업데이트됨)
        if (bookUserRepository.findByDcUser(discordUserId).isPresent()
                || adminRepository.findByDcAdmin(discordUserId).isPresent()) {
            event.reply("✅ 이미 연동된 계정입니다.").setEphemeral(true).queue();
            return;
        }

        // BookUser 매칭
        var bookUser = bookUserRepository.findByDcUser(discordUsername);
        if (bookUser.isPresent()) {
            bookUserDAO.changeDiscord(bookUser.get(), discordUserId);
            event.reply("✅ 플북 계정 연동 완료!\n**아이디:** " + bookUser.get().getIdUser())
                    .setEphemeral(true).queue();
            return;
        }

        // Admin 매칭
        var admin = adminRepository.findByDcAdmin(discordUsername);
        if (admin.isPresent()) {
            adminDAO.changeDiscord(admin.get(), discordUserId);
            event.reply("✅ 플북 관리자 계정 연동 완료!\n**아이디:** " + admin.get().getIdAdmin())
                    .setEphemeral(true).queue();
            return;
        }

        // 매칭 실패
        event.reply("❌ 플북에 등록된 계정을 찾을 수 없습니다.\n" +
                "플북 회원가입 시 입력한 디스코드 아이디를 확인해주세요.\n" +
                "현재 디스코드 아이디: `" + discordUsername + "`")
                .setEphemeral(true).queue();
    }
}
