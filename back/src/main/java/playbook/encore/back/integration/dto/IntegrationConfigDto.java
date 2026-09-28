package playbook.encore.back.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 연동 설정 단건 응답.
 * 시크릿 값은 평문 대신 마스킹(설정 여부만 표시)되어 내려간다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationConfigDto {
    private String configKey;
    private String configValue;   // 시크릿이면 마스킹된 값
    // Lombok 이 boolean isSecret 의 게터를 isSecret() 로 만들어 Jackson 이 "secret" 으로 내보낸다.
    // 프론트(IntegrationManagement.vue)는 cfg.isSecret 을 읽으므로 키 이름을 고정한다
    @JsonProperty("isSecret")
    private boolean isSecret;
    private boolean configured;   // 값이 설정되어 있는지 여부
    private String category;
    private String description;
}
