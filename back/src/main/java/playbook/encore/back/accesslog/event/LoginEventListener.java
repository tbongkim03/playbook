package playbook.encore.back.accesslog.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import playbook.encore.back.accesslog.service.AccessLogService;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginEventListener {

    private final AccessLogService accessLogService;

    @Async
    @EventListener
    public void handleLoginEvent(LoginEvent event) {
        try {
            accessLogService.saveFromEvent(event);
        } catch (Exception e) {
            log.error("[AccessLog] 접속이력 저장 실패 - actorName={}, result={}", event.getActorName(), event.getResult(), e);
        }
    }
}
