package playbook.encore.back.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 연동 테스트 결과 공용 응답.
 * - success    : 테스트 성공 여부 (디스코드 연결 초록불 판단 포함)
 * - message    : 사람이 읽을 결과 메시지
 * - detail     : 봇 권한 목록 / API 샘플 응답 등 부가 정보
 * - settingsUrl: 디스코드 봇 권한·설정 변경 링크 (디스코드 테스트에서만)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationTestResultDto {
    private boolean success;
    private String message;
    private Object detail;
    private String settingsUrl;
}
