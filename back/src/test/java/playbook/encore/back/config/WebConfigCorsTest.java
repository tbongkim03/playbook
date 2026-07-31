package playbook.encore.back.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CORS 허용 출처 설정 단위 테스트.
 *
 * <p>과거 개발서버 접근을 위해 {@code allowedOriginPatterns("*")} + {@code allowCredentials(true)} 로
 * 열어둔 적이 있다(`d39eb3b`). 임의 출처에서 세션 쿠키가 실린 요청이 가능해지므로
 * 다시 열리지 않도록 여기서 막는다.</p>
 */
@DisplayName("WebConfig CORS 설정")
class WebConfigCorsTest {

    /** {@code getCorsConfigurations()} 가 protected 라 하위 클래스로 열어준다. */
    private static class InspectableRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configs() {
            return getCorsConfigurations();
        }
    }

    private InspectableRegistry apply(String allowedOrigins) {
        WebConfig config = new WebConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", allowedOrigins);
        InspectableRegistry registry = new InspectableRegistry();
        config.addCorsMappings(registry);
        return registry;
    }

    @Test
    @DisplayName("C1 - 와일드카드 \"*\" 는 기동 시 거부된다")
    void C1_와일드카드_거부() {
        assertThatThrownBy(() -> apply("*"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("\"*\"");
    }

    @Test
    @DisplayName("C2 - 목록에 \"*\" 가 섞여 있어도 거부된다")
    void C2_와일드카드_혼재_거부() {
        assertThatThrownBy(() -> apply("http://localhost,*"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("C3 - 빈 값이면 CORS 매핑을 등록하지 않는다 (교차 출처 차단)")
    void C3_빈값_매핑_미등록() {
        assertThat(apply("").configs()).isEmpty();
        assertThat(apply("  ,  ").configs()).isEmpty();
    }

    @Test
    @DisplayName("C4 - 지정한 출처만 허용하고 자격증명을 함께 켠다")
    void C4_지정_출처만_허용() {
        Map<String, CorsConfiguration> configs =
                apply("http://localhost, http://localhost:8081").configs();

        assertThat(configs).containsOnlyKeys("/**");
        CorsConfiguration cors = configs.get("/**");
        assertThat(cors.getAllowedOriginPatterns())
                .containsExactly("http://localhost", "http://localhost:8081");
        assertThat(cors.getAllowCredentials()).isTrue();
        // 고정 목록만 쓰므로 allowedOrigins 는 비어 있어야 한다 (패턴 쪽에만 설정)
        assertThat(cors.getAllowedOrigins()).isNull();
    }
}
