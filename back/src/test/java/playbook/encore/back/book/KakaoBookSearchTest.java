package playbook.encore.back.book;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import playbook.encore.back.common.BaseIntegrationTest;
import playbook.encore.back.integration.service.IntegrationService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 카카오 책 검색(표지 보조 조회) — 네이버 책 검색 API 종료(2026-07-31)에 따른 교체.
 *
 * <p>카카오 키를 비운 상태로 고정해 외부 호출 없이 권한·키 미설정 경로만 검증한다.
 * (CI 는 더미 키를 시드하지만 {@link #clearKakaoKey()} 가 비운다)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KakaoBookSearchTest extends BaseIntegrationTest {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final Map<String, String> ISBN_BODY = Map.of("isbn", "9788966261208");

    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private IntegrationService integrationService;

    private HttpHeaders superSession;
    private HttpHeaders campusSession;
    private HttpHeaders userSession;
    /** 공유 DB(playbookdb_local)의 원래 키 — 테스트 후 복원한다 */
    private String originalKakaoValue;

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
        originalKakaoValue = jdbcTemplate.query(
                "SELECT config_value FROM tb_integration_config WHERE config_key = ?",
                rs -> rs.next() ? rs.getString(1) : null,
                IntegrationService.KEY_KAKAO_REST_API_KEY);
        clearKakaoKey();
        superSession = loginAsAdmin("test_admin_super", "Test1234!");
        campusSession = loginAsAdmin("test_admin01", "Test1234!");
        userSession = loginAsUser("test_user01", "Test1234!");
    }

    @AfterAll
    void teardown() throws Exception {
        jdbcTemplate.update("UPDATE tb_integration_config SET config_value = ? WHERE config_key = ?",
                originalKakaoValue, IntegrationService.KEY_KAKAO_REST_API_KEY);
        integrationService.reloadCache();
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_teardown.sql"));
        }
    }

    private void clearKakaoKey() {
        jdbcTemplate.update("UPDATE tb_integration_config SET config_value = '' WHERE config_key = ?",
                IntegrationService.KEY_KAKAO_REST_API_KEY);
        integrationService.reloadCache();
    }

    private JsonNode json(ResponseEntity<String> r) throws Exception {
        return OM.readTree(r.getBody());
    }

    @Test @Order(1)
    @DisplayName("K1: 비로그인 POST /kakao/book-search → 401")
    void K1_비로그인() {
        ResponseEntity<String> r = postWithSession("/kakao/book-search", ISBN_BODY, new HttpHeaders());
        assertThat(r.getStatusCode().value()).isEqualTo(401);
    }

    @Test @Order(2)
    @DisplayName("K2: USER 세션 POST /kakao/book-search → 403 / 3002")
    void K2_사용자권한() throws Exception {
        ResponseEntity<String> r = postWithSession("/kakao/book-search", ISBN_BODY, userSession);
        assertThat(r.getStatusCode().value()).isEqualTo(403);
        assertThat(json(r).get("code").asText()).isEqualTo("3002");
    }

    @Test @Order(3)
    @DisplayName("K3: 키 미설정 시 5xx 가 아니라 200 / 4005, data null — 프론트는 표지 없이 진행")
    void K3_키미설정() throws Exception {
        ResponseEntity<String> r = postWithSession("/kakao/book-search", ISBN_BODY, campusSession);
        assertThat(r.getStatusCode().value()).isEqualTo(200);
        JsonNode n = json(r);
        assertThat(n.get("code").asText()).isEqualTo("4005");
        assertThat(n.get("data") == null || n.get("data").isNull()).isTrue();
    }

    @Test @Order(4)
    @DisplayName("K4: isbn 누락 → 400")
    void K4_isbn누락() {
        ResponseEntity<String> r = postWithSession("/kakao/book-search", Map.of("isbn", ""), campusSession);
        assertThat(r.getStatusCode().value()).isEqualTo(400);
    }

    @Test @Order(5)
    @DisplayName("K5: 구 경로 POST /naver/book-search 는 더 이상 없다")
    void K5_네이버경로제거() {
        ResponseEntity<String> r = postWithSession("/naver/book-search", ISBN_BODY, campusSession);
        assertThat(r.getStatusCode().value()).isNotEqualTo(200);
    }

    @Test @Order(6)
    @DisplayName("K6: 캠퍼스 관리자 POST /integration/test/kakao → 403 / 3002 (전체관리자 전용)")
    void K6_연동테스트_캠퍼스관리자() throws Exception {
        ResponseEntity<String> r = postWithSession("/integration/test/kakao", null, campusSession);
        assertThat(r.getStatusCode().value()).isEqualTo(403);
        assertThat(json(r).get("code").asText()).isEqualTo("3002");
    }

    @Test @Order(7)
    @DisplayName("K7: 전체관리자 + 키 미설정 → success=false 와 안내 메시지")
    void K7_연동테스트_키미설정() throws Exception {
        ResponseEntity<String> r = postWithSession("/integration/test/kakao", null, superSession);
        assertThat(r.getStatusCode().value()).isEqualTo(200);
        JsonNode d = json(r).get("data");
        assertThat(d.get("success").asBoolean()).isFalse();
        assertThat(d.get("message").asText()).contains("설정되지 않았습니다");
    }

    @Test @Order(8)
    @DisplayName("K8: 연동 설정 목록에 KAKAO_REST_API_KEY 가 있고 NAVER_* 는 숨겨진다")
    void K8_연동설정목록() throws Exception {
        ResponseEntity<String> r = getWithSession("/integration/configs", superSession);
        assertThat(r.getStatusCode().value()).isEqualTo(200);
        JsonNode list = json(r).get("data");
        boolean hasKakao = false;
        for (JsonNode c : list) {
            String key = c.get("configKey").asText();
            assertThat(key).doesNotStartWith("NAVER_");
            if (IntegrationService.KEY_KAKAO_REST_API_KEY.equals(key)) {
                hasKakao = true;
                assertThat(c.get("category").asText()).isEqualTo("KAKAO");
                // 프론트가 cfg.isSecret 으로 읽는다 — "secret" 으로 새면 비밀 배지·password 입력이 꺼진다
                assertThat(c.has("isSecret")).as("JSON 키는 isSecret 이어야 한다").isTrue();
                assertThat(c.has("secret")).isFalse();
                assertThat(c.get("isSecret").asBoolean()).isTrue();
            }
        }
        assertThat(hasKakao).isTrue();
    }
}
