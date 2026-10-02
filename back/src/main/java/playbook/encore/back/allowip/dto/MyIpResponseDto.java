package playbook.encore.back.allowip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "지금 내가 어떤 IP로 들어와 있나" 확인 응답.
 * allowed / filterEnabled / bypassActive 키 이름을 @JsonProperty 로 고정한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyIpResponseDto {

    private String clientIp;

    /** 현재 규칙 기준으로 이 IP가 통과하는지 (필터 비활성 여부와 무관하게 판정) */
    @JsonProperty("allowed")
    private Boolean allowed;

    private Integer matchedRuleSeq;
    private String matchedRuleValue;

    /** playbook.ip-allowlist.enabled 현재값 */
    @JsonProperty("filterEnabled")
    private Boolean filterEnabled;

    /** 활성 규칙 수. 0 이면 전면 허용(fail-open) 상태 */
    private Integer activeRuleCount;

    /** 비상 우회(IP_ALLOWLIST_BYPASS)가 설정된 상태인지. 값 자체는 노출하지 않는다 */
    @JsonProperty("bypassActive")
    private Boolean bypassActive;

    /** clientIp 의 /24(IPv6는 /64) 제안 대역 */
    private String suggestedCidr;
}
