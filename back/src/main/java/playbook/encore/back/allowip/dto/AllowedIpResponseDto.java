package playbook.encore.back.allowip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 허용 IP 규칙 단건 응답.
 *
 * <p>isActive / isSystem / matchedByRequester 는 Jackson 이 키를 active/system 으로
 * 깎아버리지 않도록 @JsonProperty 로 이름을 고정한다 (계약서 boolean 직렬화 규약).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowedIpResponseDto {

    private Integer seqAllowedIp;
    private String ipValue;
    /** SINGLE | CIDR */
    private String ipType;
    /** null = 전역 규칙 */
    private Integer seqCampus;
    private String campusName;
    private String description;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("isSystem")
    private Boolean isSystem;

    /** 이 규칙이 현재 요청 IP를 포함하는지 (프론트 "내가 지금 이걸로 들어와 있음" 배지) */
    @JsonProperty("matchedByRequester")
    private Boolean matchedByRequester;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
