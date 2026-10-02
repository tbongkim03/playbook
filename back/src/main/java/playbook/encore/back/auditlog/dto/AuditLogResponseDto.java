package playbook.encore.back.auditlog.dto;

import lombok.Builder;
import lombok.Getter;
import playbook.encore.back.auditlog.entity.AuditLog;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResponseDto {

    private Long seqAuditLog;
    private String actorName;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private String result;
    private String failReason;
    private LocalDateTime auditedAt;

    public static AuditLogResponseDto from(AuditLog log) {
        return AuditLogResponseDto.builder()
                .seqAuditLog(log.getSeqAuditLog())
                .actorName(log.getActorName())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .detail(log.getDetail())
                .result(log.getResult())
                .failReason(log.getFailReason())
                .auditedAt(log.getAuditedAt())
                .build();
    }
}
