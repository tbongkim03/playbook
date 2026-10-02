package playbook.encore.back.auditlog.service;

import org.springframework.data.domain.Page;
import playbook.encore.back.auditlog.dto.AuditLogResponseDto;

public interface AuditLogService {

    void save(Long actorId, String actorName, String action,
              String targetType, String targetId, String detail,
              String result, String failReason);

    Page<AuditLogResponseDto> getAuditLogs(String action, String targetType, int page, int size);
}
