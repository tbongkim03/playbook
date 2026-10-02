package playbook.encore.back.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 중앙 모니터링 토큰 발급 API(POST /auth/token) 200 응답.
 * <pre>{ "access_token": "...", "token_type": "Bearer", "expires_at": "2026-07-03T00:00:00Z" }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MonitoringTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_at") String expiresAt) {
}
