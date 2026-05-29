package playbook.encore.back.listener;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import playbook.encore.back.admin.dao.AdminDAO;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.bookUser.dao.BookUserDAO;
import playbook.encore.back.bookUser.dao.BookUserRepository;
import playbook.encore.back.bookUser.entity.BookUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaybookListener Slash Command 권한 테스트")
class PlaybookListenerTest {

    @Mock private BookUserRepository bookUserRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private BookUserDAO bookUserDAO;
    @Mock private AdminDAO adminDAO;

    @InjectMocks private PlaybookListener listener;

    @Mock private SlashCommandInteractionEvent event;
    @Mock private User discordUser;
    @Mock private ReplyCallbackAction replyAction;

    @BeforeEach
    void setUp() {
        given(event.getUser()).willReturn(discordUser);
        given(event.reply(anyString())).willReturn(replyAction);
        given(replyAction.setEphemeral(anyBoolean())).willReturn(replyAction);
        doNothing().when(replyAction).queue();
    }

    // ──────────────────────────────────────────────
    // /findid
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("/findid")
    class FindId {

        @BeforeEach
        void setUp() {
            given(event.getName()).willReturn("findid");
        }

        @Test
        @DisplayName("snowflake ID로 연동된 계정 조회 성공")
        void 연동된_snowflakeId로_조회_성공() {
            given(discordUser.getId()).willReturn("111222333");
            given(discordUser.getName()).willReturn("testuser");

            BookUser user = buildUser("john", "john123", "111222333");
            given(bookUserRepository.findByDcUser("111222333")).willReturn(Optional.of(user));

            listener.onSlashCommandInteraction(event);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(event).reply(captor.capture());
            assertThat(captor.getValue()).contains("john123").contains("john");
        }

        @Test
        @DisplayName("username으로 조회 성공 (snowflake 연동 전)")
        void username으로_조회_성공() {
            given(discordUser.getId()).willReturn("999888777");
            given(discordUser.getName()).willReturn("testuser");

            given(bookUserRepository.findByDcUser("999888777")).willReturn(Optional.empty());
            BookUser user = buildUser("park", "park456", "testuser");
            given(bookUserRepository.findByDcUser("testuser")).willReturn(Optional.of(user));

            listener.onSlashCommandInteraction(event);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(event).reply(captor.capture());
            assertThat(captor.getValue()).contains("park456").contains("park");
        }

        @Test
        @DisplayName("미등록 디스코드 계정 → 안내 메시지 반환")
        void 미등록_계정_조회_실패() {
            given(discordUser.getId()).willReturn("000000000");
            given(discordUser.getName()).willReturn("unknown");

            given(bookUserRepository.findByDcUser("000000000")).willReturn(Optional.empty());
            given(bookUserRepository.findByDcUser("unknown")).willReturn(Optional.empty());

            listener.onSlashCommandInteraction(event);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(event).reply(captor.capture());
            assertThat(captor.getValue()).contains("❌");
        }
    }

    // ──────────────────────────────────────────────
    // /resetpw — 권한 검증 핵심
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("/resetpw 권한 검증")
    class ResetPw {

        @Mock private OptionMapping idOption;

        @BeforeEach
        void setUp() {
            given(event.getName()).willReturn("resetpw");
            given(event.getOption("id")).willReturn(idOption);
        }

        @Test
        @DisplayName("본인 계정 — snowflake ID 일치 → 초기화 성공")
        void 본인확인_snowflakeId_성공() {
            given(discordUser.getId()).willReturn("111222333");
            given(discordUser.getName()).willReturn("testuser");
            given(idOption.getAsString()).willReturn("john123");

            BookUser target = buildUser("john", "john123", "111222333");
            given(bookUserRepository.findByIdUser("john123")).willReturn(Optional.of(target));
            given(bookUserRepository.save(any())).willReturn(target);

            listener.onSlashCommandInteraction(event);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(event).reply(captor.capture());
            assertThat(captor.getValue()).contains("✅").contains("임시 비밀번호");
            verify(bookUserRepository).save(target);
        }

        @Test
        @DisplayName("본인 계정 — username 일치 → 초기화 성공 (snowflake 연동 전)")
        void 본인확인_username_성공() {
            given(discordUser.getId()).willReturn("999888777");
            given(discordUser.getName()).willReturn("testuser");
            given(idOption.getAsString()).willReturn("park456");

            // dcUser가 username으로 저장된 상태
            BookUser target = buildUser("park", "park456", "testuser");
            given(bookUserRepository.findByIdUser("park456")).willReturn(Optional.of(target));
            given(bookUserRepository.save(any())).willReturn(target);

            listener.onSlashCommandInteraction(event);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(event).reply(captor.capture());
            assertThat(captor.getValue()).contains("✅").contains("임시 비밀번호");
        }

        @Test
        @DisplayName("타인 계정 초기화 시도 → 권한 거부 메시지")
        void 타인_계정_초기화_권한_거부() {
            // 요청자: discordId=ATTACKER, name=attacker
            given(discordUser.getId()).willReturn("ATTACKER_ID");
            given(discordUser.getName()).willReturn("attacker");
            given(idOption.getAsString()).willReturn("victim123");

            // 피해자 계정: dcUser가 전혀 다른 디스코드 ID
            BookUser victim = buildUser("victim", "victim123", "VICTIM_ID");
            given(bookUserRepository.findByIdUser("victim123")).willReturn(Optional.of(victim));

            listener.onSlashCommandInteraction(event);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(event).reply(captor.capture());
            assertThat(captor.getValue()).contains("❌").contains("본인 계정만");
            // 비밀번호가 변경되어선 안 됨
            verify(bookUserRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 플북 아이디 → 오류 메시지")
        void 존재하지_않는_아이디() {
            given(discordUser.getId()).willReturn("111222333");
            given(discordUser.getName()).willReturn("testuser");
            given(idOption.getAsString()).willReturn("ghost999");

            given(bookUserRepository.findByIdUser("ghost999")).willReturn(Optional.empty());

            listener.onSlashCommandInteraction(event);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(event).reply(captor.capture());
            assertThat(captor.getValue()).contains("❌").contains("ghost999");
            verify(bookUserRepository, never()).save(any());
        }

        @Test
        @DisplayName("임시 비밀번호는 BCrypt 해시로 저장되어야 함")
        void 임시비밀번호_bcrypt_해시_저장() {
            given(discordUser.getId()).willReturn("111222333");
            given(discordUser.getName()).willReturn("testuser");
            given(idOption.getAsString()).willReturn("john123");

            BookUser target = buildUser("john", "john123", "111222333");
            given(bookUserRepository.findByIdUser("john123")).willReturn(Optional.of(target));

            ArgumentCaptor<BookUser> savedCaptor = ArgumentCaptor.forClass(BookUser.class);
            given(bookUserRepository.save(savedCaptor.capture())).willReturn(target);

            listener.onSlashCommandInteraction(event);

            String savedPw = savedCaptor.getValue().getPwUser();
            // BCrypt 해시는 $2a$ 또는 $2b$ 로 시작
            assertThat(savedPw).startsWith("$2");
            // 원래 평문이 아닌 해시가 저장됨
            assertThat(savedPw).isNotEqualTo("john123");
        }
    }

    private BookUser buildUser(String name, String id, String dcUser) {
        return BookUser.builder()
                .nameUser(name)
                .idUser(id)
                .pwUser("hashed_pw")
                .dcUser(dcUser)
                .agreeTermsUser(true)
                .agreeInfoUser(true)
                .agreeDiscordAlarmUser(false)
                .statusUser(BookUser.StatusType.available)
                .build();
    }
}
