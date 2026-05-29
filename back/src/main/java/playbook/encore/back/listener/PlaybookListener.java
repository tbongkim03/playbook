package playbook.encore.back.listener;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import playbook.encore.back.admin.dao.AdminDAO;
import playbook.encore.back.bookUser.dao.BookUserDAO;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.bookUser.dao.BookUserRepository;
import playbook.encore.back.bookUser.entity.BookUser;

import java.security.SecureRandom;
import java.util.Optional;

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
    private static final String CMD_FIND_ID = "findid";
    private static final String CMD_RESET_PW = "resetpw";

    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().upsertCommand(
            Commands.slash(CMD_FIND_ID, "플북 아이디를 찾습니다 (본인 디스코드 연동 계정 기준)")
        ).queue();

        event.getJDA().upsertCommand(
            Commands.slash(CMD_RESET_PW, "플북 임시 비밀번호를 발급합니다")
                .addOption(OptionType.STRING, "id", "플북 아이디를 입력하세요", true)
        ).queue();
    }

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

        if (bookUserRepository.findByDcUser(discordUserId).isPresent()
                || adminRepository.findByDcAdmin(discordUserId).isPresent()) {
            event.reply("✅ 이미 연동된 계정입니다.").setEphemeral(true).queue();
            return;
        }

        var bookUser = bookUserRepository.findByDcUser(discordUsername);
        if (bookUser.isPresent()) {
            bookUserDAO.changeDiscord(bookUser.get(), discordUserId);
            event.reply("✅ 플북 계정 연동 완료!\n**아이디:** " + bookUser.get().getIdUser())
                    .setEphemeral(true).queue();
            return;
        }

        var admin = adminRepository.findByDcAdmin(discordUsername);
        if (admin.isPresent()) {
            adminDAO.changeDiscord(admin.get(), discordUserId);
            event.reply("✅ 플북 관리자 계정 연동 완료!\n**아이디:** " + admin.get().getIdAdmin())
                    .setEphemeral(true).queue();
            return;
        }

        event.reply("❌ 플북에 등록된 계정을 찾을 수 없습니다.\n" +
                "플북 회원가입 시 입력한 디스코드 아이디를 확인해주세요.\n" +
                "현재 디스코드 아이디: `" + discordUsername + "`")
                .setEphemeral(true).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case CMD_FIND_ID -> handleFindId(event);
            case CMD_RESET_PW -> handleResetPw(event);
        }
    }

    private void handleFindId(SlashCommandInteractionEvent event) {
        String discordUserId = event.getUser().getId();
        String discordUsername = event.getUser().getName();

        // 연동된 계정 우선 조회 (snowflake ID)
        Optional<BookUser> user = bookUserRepository.findByDcUser(discordUserId);

        // 연동 전 username으로 조회
        if (user.isEmpty()) {
            user = bookUserRepository.findByDcUser(discordUsername);
        }

        if (user.isEmpty()) {
            event.reply("❌ 등록된 플북 계정을 찾을 수 없습니다.\n" +
                    "플북 회원가입 시 입력한 디스코드 아이디와 현재 계정이 일치하는지 확인하거나,\n" +
                    "디스코드 연동 채널에서 연동을 먼저 진행해주세요.")
                    .setEphemeral(true).queue();
            return;
        }

        BookUser found = user.get();
        event.reply("📚 **플북 아이디 안내**\n" +
                "이름: **" + found.getNameUser() + "**\n" +
                "아이디: **" + found.getIdUser() + "**\n\n" +
                "> 비밀번호를 잊으셨다면 `/resetpw id:" + found.getIdUser() + "` 를 입력하세요.")
                .setEphemeral(true).queue();
    }

    private void handleResetPw(SlashCommandInteractionEvent event) {
        String playbookId = event.getOption("id").getAsString().trim();
        String discordUserId = event.getUser().getId();
        String discordUsername = event.getUser().getName();

        Optional<BookUser> userOpt = bookUserRepository.findByIdUser(playbookId);
        if (userOpt.isEmpty()) {
            event.reply("❌ `" + playbookId + "` 아이디를 찾을 수 없습니다.")
                    .setEphemeral(true).queue();
            return;
        }

        BookUser target = userOpt.get();
        String storedDc = target.getDcUser();

        // snowflake ID 또는 username 으로 본인 확인
        boolean isOwner = storedDc.equals(discordUserId) || storedDc.equals(discordUsername);
        if (!isOwner) {
            event.reply("❌ 본인 계정만 비밀번호를 초기화할 수 있습니다.\n" +
                    "현재 디스코드 계정과 플북 등록 디스코드가 일치하지 않습니다.")
                    .setEphemeral(true).queue();
            return;
        }

        String tempPassword = generateTempPassword();
        target.setPwUser(BCrypt.hashpw(tempPassword, BCrypt.gensalt()));
        bookUserRepository.save(target);

        event.reply("✅ **임시 비밀번호 발급 완료**\n" +
                "임시 비밀번호: **`" + tempPassword + "`**\n\n" +
                "> ⚠️ 로그인 후 마이페이지에서 반드시 비밀번호를 변경해주세요.\n" +
                "> 이 메시지는 본인에게만 보입니다.")
                .setEphemeral(true).queue();
    }

    // 영문 소문자 + 숫자 조합 6자리 임시 비밀번호 생성 (정책: 4-8자, 영문+숫자 포함)
    private String generateTempPassword() {
        SecureRandom random = new SecureRandom();
        String letters = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        StringBuilder sb = new StringBuilder();
        // 최소 2자 영문, 2자 숫자 보장 후 나머지 2자 혼합
        for (int i = 0; i < 2; i++) sb.append(letters.charAt(random.nextInt(letters.length())));
        for (int i = 0; i < 2; i++) sb.append(digits.charAt(random.nextInt(digits.length())));
        String pool = letters + digits;
        for (int i = 0; i < 2; i++) sb.append(pool.charAt(random.nextInt(pool.length())));
        // Fisher-Yates 셔플
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
        }
        return new String(chars);
    }
}
