package playbook.encore.back.accesslog.service;

import org.springframework.data.domain.Page;
import playbook.encore.back.accesslog.dto.AccessLogResponseDto;
import playbook.encore.back.accesslog.event.LoginEvent;

public interface AccessLogService {

    void saveFromEvent(LoginEvent event);

    Page<AccessLogResponseDto> getAccessLogs(String actorType, String result, int page, int size);
}
