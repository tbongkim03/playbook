package playbook.encore.back.auditlog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.auditlog.dao.AuditLogRepository;
import playbook.encore.back.auditlog.dto.AuditLogResponseDto;
import playbook.encore.back.auditlog.entity.AuditLog;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void save(Long actorId, String actorName, String action,
                     String targetType, String targetId, String detail,
                     String result, String failReason) {
        AuditLog auditLog = AuditLog.builder()
                .actorId(actorId)
                .actorName(actorName)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail)
                .result(result)
                .failReason(failReason)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getAuditLogs(String action, String targetType, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<AuditLog> logs;
        if (action != null && targetType != null) {
            logs = auditLogRepository.findByActionAndTargetTypeOrderByAuditedAtDesc(action, targetType, pageable);
        } else if (action != null) {
            logs = auditLogRepository.findByActionOrderByAuditedAtDesc(action, pageable);
        } else if (targetType != null) {
            logs = auditLogRepository.findByTargetTypeOrderByAuditedAtDesc(targetType, pageable);
        } else {
            logs = auditLogRepository.findAllByOrderByAuditedAtDesc(pageable);
        }

        return logs.map(AuditLogResponseDto::from);
    }
}
