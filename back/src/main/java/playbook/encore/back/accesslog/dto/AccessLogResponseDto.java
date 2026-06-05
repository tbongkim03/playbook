package playbook.encore.back.accesslog.dto;

import lombok.Builder;
import lombok.Getter;
import playbook.encore.back.accesslog.entity.AccessLog;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccessLogResponseDto {

    private Long seqAccessLog;
    private String actorType;
    private String actorName;
    private String ipAddress;
    private String result;
    private String failReason;
    private LocalDateTime accessedAt;

    public static AccessLogResponseDto from(AccessLog log) {
        return AccessLogResponseDto.builder()
                .seqAccessLog(log.getSeqAccessLog())
                .actorType(log.getActorType())
                .actorName(log.getActorName())
                .ipAddress(log.getIpAddress())
                .result(log.getResult())
                .failReason(log.getFailReason())
                .accessedAt(log.getAccessedAt())
                .build();
    }
}
