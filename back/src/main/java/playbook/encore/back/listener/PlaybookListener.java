package playbook.encore.back.listener;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.AdminDAO;
import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.data.repository.AdminRepository;
import playbook.encore.back.data.repository.BookUserRepository;

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

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String discordUsername = event.getUser().getName(); // @nickname 형태
        String discordUserId = event.getUser().getId(); // 숫자 형태의 실제 Discord ID

        // BookUser 테이블에서 디스코드 아이디로 사용자 찾기
        bookUserRepository.findByDcUser(discordUsername).ifPresent(user -> {
            // 실제 Discord User ID로 업데이트
            user.setDcUser(discordUserId);
            bookUserDAO.changeDiscord(user, discordUserId);
        });

        // Admin 테이블에서도 동일하게 처리
        adminRepository.findByDcAdmin(discordUsername).ifPresent(admin -> {
            admin.setDcAdmin(discordUserId);
            adminDAO.changeDiscord(admin, discordUserId);
        });
    }
}
