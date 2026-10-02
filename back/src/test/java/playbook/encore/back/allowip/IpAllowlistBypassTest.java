package playbook.encore.back.allowip;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 비상 우회(IP_ALLOWLIST_BYPASS) 실동작 검증 — 잠김 방지 ③.
 *
 * <p>캠퍼스에 IT 담당자가 없으므로, 잘못된 규칙으로 관리자가 스스로 잠겼을 때
 * <b>.env 수정 + 컨테이너 재시작만으로</b> 복구되어야 한다. 이 장치가 동작하지 않으면
 * 현장에서 복구할 방법이 사실상 없다. 그래서 별도 컨텍스트로 실제 검증한다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "playbook.ip-allowlist.enabled=true",
        "playbook.ip-allowlist.trusted-proxies=127.0.0.1",
        "playbook.ip-allowlist.internal-networks=172.16.0.0/12",
        "playbook.ip-allowlist.bypass=203.0.113.0/24"
})
class IpAllowlistBypassTest extends BaseAllowedIpTest {

    /** 비상 우회 대역 안 */
    private static final String RESCUED = "203.0.113.77";
    /** 우회에도 규칙에도 걸리지 않는 IP */
    private static final String STILL_BLOCKED = "192.0.2.77";

    @Autowired
    private DataSource dataSource;

    private HttpHeaders superSession;

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM tb_allowed_ip");
        }
        superSession = loginAsAdmin("test_admin_super", "Test1234!");

        // "관리자가 스스로 잠긴" 상태를 만든다: 아무도 매칭되지 않는 규칙 1건만 활성
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"198.51.100.0/24\",\"seqCampus\":null,\"description\":\"TEST_잠김유발\"}",
                superSession, null);
        assertThat(r.status()).isEqualTo(200);
    }

    @AfterAll
    void teardown() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_teardown.sql"));
        }
    }

    @Test @Order(1)
    @DisplayName("AIPB1: 우회 대역 밖 IP는 정상적으로 차단된다 (대조군)")
    void AIPB1_대조군_차단() {
        assertThat(call("GET", "/ip-gate", null, null, STILL_BLOCKED).status()).isEqualTo(403);
    }

    @Test @Order(2)
    @DisplayName("AIPB2: IP_ALLOWLIST_BYPASS 대역은 규칙과 무관하게 통과한다 (잠김 복구 경로)")
    void AIPB2_우회대역_통과() {
        assertThat(call("GET", "/ip-gate", null, null, RESCUED).status())
                .as("비상 우회가 동작하지 않으면 잠긴 캠퍼스를 현장에서 복구할 수 없다").isEqualTo(204);
        assertThat(call("GET", "/allowed-ips", null, superSession, RESCUED).status()).isEqualTo(200);
    }

    @Test @Order(3)
    @DisplayName("AIPB3: my-ip 가 bypassActive=true 를 알려준다 (프론트 보조 배너)")
    void AIPB3_myIp_bypassActive() {
        Resp r = call("GET", "/allowed-ips/my-ip", null, superSession, RESCUED);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("bypassActive").asBoolean()).isTrue();
        assertThat(d.get("filterEnabled").asBoolean()).isTrue();
        assertThat(d.get("clientIp").asText()).isEqualTo(RESCUED);
    }

    @Test @Order(4)
    @DisplayName("AIPB4: 우회로 들어온 요청자는 자기차단 검사를 건너뛴다 (복구 중 삭제가 막히면 안 된다)")
    void AIPB4_우회요청자_자기차단검사_스킵() {
        Resp list = call("GET", "/allowed-ips", null, superSession, RESCUED);
        int seq = data(list).get(0).get("seqAllowedIp").asInt();
        Resp del = call("DELETE", "/allowed-ips/" + seq, null, superSession, RESCUED);
        assertThat(del.status()).as("복구 작업이 4002 로 막히면 안 된다").isEqualTo(200);
    }
}
