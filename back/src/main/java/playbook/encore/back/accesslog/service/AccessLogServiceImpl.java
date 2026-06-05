package playbook.encore.back.accesslog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.accesslog.dao.AccessLogRepository;
import playbook.encore.back.accesslog.dto.AccessLogResponseDto;
import playbook.encore.back.accesslog.entity.AccessLog;
import playbook.encore.back.accesslog.event.LoginEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessLogServiceImpl implements AccessLogService {

    private final AccessLogRepository accessLogRepository;

    @Override
    @Transactional
    public void saveFromEvent(LoginEvent event) {
        AccessLog log = AccessLog.builder()
                .actorType(event.getActorType())
                .actorId(event.getActorId())
                .actorName(event.getActorName())
                .ipAddress(event.getIpAddress())
                .result(event.getResult())
                .failReason(event.getFailReason())
                .build();
        accessLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccessLogResponseDto> getAccessLogs(String actorType, String result, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<AccessLog> logs;
        if (actorType != null && result != null) {
            logs = accessLogRepository.findByActorTypeAndResultOrderByAccessedAtDesc(actorType, result, pageable);
        } else if (actorType != null) {
            logs = accessLogRepository.findByActorTypeOrderByAccessedAtDesc(actorType, pageable);
        } else if (result != null) {
            logs = accessLogRepository.findByResultOrderByAccessedAtDesc(result, pageable);
        } else {
            logs = accessLogRepository.findAllByOrderByAccessedAtDesc(pageable);
        }

        return logs.map(AccessLogResponseDto::from);
    }
}
