package playbook.encore.back.integration.dto;

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
    private boolean isSecret;
    private boolean configured;   // 값이 설정되어 있는지 여부
    private String category;
    private String description;
}
