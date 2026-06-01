package playbook.encore.back.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscordNotificationService 단위 테스트")
class DiscordNotificationServiceTest {

    @Mock private JDA jda;
    @InjectMocks private DiscordNotificationService service;

    // DM 체인 mock
    @Mock private User discordUser;
    @Mock private PrivateChannel privateChannel;
    @Mock private MessageCreateAction dmMessageAction;

    @SuppressWarnings("rawtypes")
    @Mock private CacheRestAction retrieveUserAction;

    @SuppressWarnings("rawtypes")
    @Mock private CacheRestAction openChannelAction;

    // 채널 메시지 mock
    @Mock private TextChannel textChannel;
    @Mock private MessageCreateAction channelMessageAction;

    @BeforeEach
    void setUp() {
        given(jda.getStatus()).willReturn(JDA.Status.CONNECTED);
    }

    // ─── DM 체인 공통 셋업 ─────────────────────────────
    @SuppressWarnings("unchecked")
    private void setupDmChain(String discordUserId) {
        given(jda.retrieveUserById(discordUserId)).willReturn(retrieveUserAction);
        doAnswer(inv -> {
            Consumer<User> consumer = inv.getArgument(0);
            consumer.accept(discordUser);
            return null;
        }).when(retrieveUserAction).queue(any());

        given(discordUser.openPrivateChannel()).willReturn(openChannelAction);
        doAnswer(inv -> {
            Consumer<PrivateChannel> consumer = inv.getArgument(0);
            consumer.accept(privateChannel);
            return null;
        }).when(openChannelAction).queue(any());

        given(privateChannel.sendMessage(anyString())).willReturn(dmMessageAction);
    }

    // ─── D1 ────────────────────────────────────────────
    @Test
    @DisplayName("D1 - 대출 알림 전송: privateChannel.sendMessage 호출 확인")
    void D1_대출알림_전송_성공() {
        setupDmChain("111222333");

        service.sendBorrowNotification("111222333", "김철수", "클린코드", "2026-06-15");

        verify(privateChannel).sendMessage(anyString());
        verify(dmMessageAction).queue();
    }

    // ─── D2 ────────────────────────────────────────────
    @Test
    @DisplayName("D2 - 반납 알림 전송: privateChannel.sendMessage 호출 확인")
    void D2_반납알림_전송_성공() {
        setupDmChain("111222333");

        service.sendReturnNotification("111222333", "김철수", "클린코드");

        verify(privateChannel).sendMessage(anyString());
        verify(dmMessageAction).queue();
    }

    // ─── D3 ────────────────────────────────────────────
    @Test
    @DisplayName("D3 - 링크 버튼 메시지 전송: channel.sendMessage + addActionRow 호출 확인")
    void D3_링크버튼_메시지_전송() {
        given(jda.getTextChannelById("LINK_CH_001")).willReturn(textChannel);
        given(textChannel.sendMessage(anyString())).willReturn(channelMessageAction);
        doReturn(channelMessageAction).when(channelMessageAction).addActionRow(any(ItemComponent.class));

        service.sendLinkButtonMessage("LINK_CH_001");

        verify(textChannel).sendMessage(anyString());
        verify(channelMessageAction).addActionRow(any(ItemComponent.class));
        verify(channelMessageAction).queue();
    }
}
