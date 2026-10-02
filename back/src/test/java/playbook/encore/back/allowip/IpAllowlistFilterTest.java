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
 * IpAllowlistFilter 실동작 검증 — <b>이 클래스에서만</b> 차단을 켠다.
 *
 * <p>전역으로 켜면 다른 테스트가 자기 자신을 잠근다. 여기서도 테스트 러너는 127.0.0.1(루프백)에서
 * 접속하므로 잠기지 않으며, 차단 상황은 {@code X-Forwarded-For} 로 클라이언트 IP를 가장해 재현한다
 * (신뢰 프록시 = 127.0.0.1, application-test.properties).
 *
 * <p>검증 핵심은 <b>잠김 방지 fail-open</b>이다. 특히 "활성 규칙 0건 → 전면 허용"(⑤)과
 * 루프백·Docker 내부망 항상 허용(②·④)이 깨지면 캠퍼스가 통째로 잠기고 현장 복구가 불가능하다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "playbook.ip-allowlist.enabled=true",
        "playbook.ip-allowlist.trusted-proxies=127.0.0.1",
        "playbook.ip-allowlist.internal-networks=172.16.0.0/12"
})
class IpAllowlistFilterTest extends BaseAllowedIpTest {

    /** 허용 규칙에 포함되지 않는 외부 IP (RFC 5737) */
    private static final String OUTSIDER = "203.0.113.9";
    /** 허용 대역 안의 IP */
    private static final String INSIDER = "198.51.100.7";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private io.micrometer.core.instrument.MeterRegistry meterRegistry;

    private HttpHeaders superSession;
    private static Integer seqRule;

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
        wipeAllowedIps();
        superSession = loginAsAdmin("test_admin_super", "Test1234!");
        assertThat(superSession.get(HttpHeaders.COOKIE)).as("전체관리자 로그인 실패").isNotNull();
    }

    @AfterAll
    void teardown() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_teardown.sql"));
        }
    }

    private void wipeAllowedIps() throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM tb_allowed_ip");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // 잠김 방지 — 규칙 0건 상태
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(1)
    @DisplayName("AIPF1: 필터가 켜져 있다 (my-ip.filterEnabled=true)")
    void AIPF1_필터_활성확인() {
        Resp r = call("GET", "/allowed-ips/my-ip", null, superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("filterEnabled").asBoolean()).isTrue();
        assertThat(d.get("activeRuleCount").asInt()).isZero();
    }

    @Test @Order(2)
    @DisplayName("AIPF2: 활성 규칙 0건 → 외부 IP 도 통과 (잠김 방지 ⑤ · 마이그레이션 직후 잠김 불가)")
    void AIPF2_규칙0건_전면허용() {
        Resp gate = call("GET", "/ip-gate", null, null, OUTSIDER);
        assertThat(gate.status()).as("규칙 0건인데 차단됐다 — 캠퍼스가 통째로 잠긴다").isEqualTo(204);

        Resp api = call("GET", "/campus", null, null, OUTSIDER);
        assertThat(api.status()).isEqualTo(200);
    }

    // ══════════════════════════════════════════════════════════════════════
    // 규칙 1건 상태 — 실제 차단
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(10)
    @DisplayName("AIPF10: (준비) 198.51.100.0/24 전역 규칙 등록 — 등록 요청 자체는 루프백이라 통과")
    void AIPF10_규칙등록() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"198.51.100.0/24\",\"seqCampus\":null,\"description\":\"TEST_필터대역\"}",
                superSession, null);
        assertThat(r.status()).isEqualTo(200);
        seqRule = data(r).get("seqAllowedIp").asInt();
    }

    @Test @Order(11)
    @DisplayName("AIPF11: 미허용 IP → /ip-gate 403 (nginx auth_request 가 거부로 해석)")
    void AIPF11_미허용IP_게이트차단() {
        Resp r = call("GET", "/ip-gate", null, null, OUTSIDER);
        assertThat(r.status()).isEqualTo(403);
    }

    @Test @Order(12)
    @DisplayName("AIPF12: 차단 응답은 JSON 이 아니라 text/plain 한국어 평문 (브라우저가 직접 읽는다)")
    void AIPF12_차단응답_형식() {
        Resp r = call("GET", "/campus", null, null, OUTSIDER);
        assertThat(r.status()).isEqualTo(403);
        assertThat(r.contentType().toLowerCase()).contains("text/plain");
        assertThat(r.contentType().toLowerCase()).contains("utf-8");
        assertThat(r.body()).isEqualTo("허용되지 않은 접속 위치입니다. (IP: " + OUTSIDER + ")");
    }

    @Test @Order(13)
    @DisplayName("AIPF13: 필터는 인터셉터보다 앞이라 인증된 요청도 차단된다")
    void AIPF13_인증된요청도_차단() {
        Resp r = call("GET", "/allowed-ips", null, superSession, OUTSIDER);
        assertThat(r.status()).isEqualTo(403);
        assertThat(r.body()).contains("허용되지 않은 접속 위치입니다");
    }

    @Test @Order(14)
    @DisplayName("AIPF14: 허용 대역 안의 IP → 통과")
    void AIPF14_허용IP_통과() {
        assertThat(call("GET", "/ip-gate", null, null, INSIDER).status()).isEqualTo(204);
        assertThat(call("GET", "/campus", null, null, INSIDER).status()).isEqualTo(200);
    }

    @Test @Order(15)
    @DisplayName("AIPF15: 루프백은 규칙과 무관하게 항상 통과 (잠김 방지 ②)")
    void AIPF15_루프백_항상통과() {
        assertThat(call("GET", "/ip-gate", null, null, null).status()).isEqualTo(204);
        assertThat(call("GET", "/allowed-ips", null, superSession, null).status()).isEqualTo(200);
    }

    @Test @Order(16)
    @DisplayName("AIPF16: Docker 내부망(172.16.0.0/12)은 항상 통과 (잠김 방지 ④)")
    void AIPF16_내부망_항상통과() {
        assertThat(call("GET", "/ip-gate", null, null, "172.20.0.5").status()).isEqualTo(204);
    }

    @Test @Order(17)
    @DisplayName("AIPF17: XFF 는 마지막 항목만 신뢰한다 (앞에 허용 IP를 붙여 우회할 수 없다)")
    void AIPF17_XFF_마지막항목만_신뢰() {
        // 마지막 항목이 허용 대역이면 앞에 무엇이 붙어 있어도 통과
        assertThat(call("GET", "/ip-gate", null, null, OUTSIDER + ", " + INSIDER).status()).isEqualTo(204);
        // 마지막 항목이 미허용이면 앞에 허용 IP를 끼워 넣어도 차단 (위조 방지)
        assertThat(call("GET", "/ip-gate", null, null, INSIDER + ", " + OUTSIDER).status()).isEqualTo(403);
    }

    @Test @Order(18)
    @DisplayName("AIPF18: 내부망 통과 시 allowed=true 인데 matchedRuleSeq 는 null 이다 (프론트 배너 '규칙 #null')")
    void AIPF18_myIp_matchedRuleSeq_null() {
        // my-ip 요청 자체를 외부 IP로 가장하면 필터가 먼저 403 을 낸다.
        // 그래서 "차단되지 않지만 어떤 규칙에도 매칭되지 않는" 내부망 IP로 확인한다.
        // 이 조합은 prod 에서 trusted-proxies 설정이 어긋나 clientIp 가 Docker 브리지 IP로 보일 때 그대로 발생한다.
        Resp r = call("GET", "/allowed-ips/my-ip", null, superSession, "172.20.0.5");
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("clientIp").asText()).isEqualTo("172.20.0.5");
        assertThat(d.get("filterEnabled").asBoolean()).isTrue();
        assertThat(d.get("activeRuleCount").asInt()).isEqualTo(1);

        // 프론트 bannerText 는 filterEnabled && activeRuleCount>0 && allowed 이면
        // `규칙 #${matchedRuleSeq} 로 허용됨` 을 출력한다 → 여기서 '규칙 #null 로 허용됨' 이 된다.
        assertThat(d.get("allowed").asBoolean()).isTrue();
        assertThat(d.get("matchedRuleSeq").isNull())
                .as("allowed=true + matchedRuleSeq=null 조합 — 프론트 배너 문구가 '규칙 #null' 이 된다").isTrue();
    }

    @Test @Order(19)
    @DisplayName("AIPF19: 위조된 XFF 로는 차단을 우회할 수 없다 (보안감사 S-1 회귀 방지)")
    void AIPF19_XFF_위조_우회불가() {
        // 회귀 방지 테스트다. 예전 nginx-prod.conf 는
        // `proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;` 를 썼는데,
        // 이 변수는 "클라이언트가 보낸 XFF" 뒤에 $remote_addr 를 덧붙이므로
        // 첫 항목이 클라이언트가 마음대로 정한 값이 됐다.
        // 그때 백엔드가 첫 항목을 신뢰해서 `curl -H "X-Forwarded-For: 127.0.0.1"` 한 줄로
        // 차단이 전면 무력화됐다 (CRITICAL).
        //
        // 지금은 두 겹으로 막는다:
        //   1) nginx 가 X-Forwarded-For 를 $remote_addr 로 덮어써 클라이언트 값을 버린다
        //   2) 백엔드가 마지막 항목만 신뢰한다 (nginx 설정이 회귀해도 안전)
        // 아래는 1)이 회귀해 클라이언트 값이 살아 들어온 상황을 재현한 것이다.
        String forgedByClient = INSIDER;           // 공격자가 헤더에 직접 넣은 허용 IP
        String realClient = OUTSIDER;              // nginx 가 뒤에 덧붙인 실제 접속 IP
        Resp r = call("GET", "/ip-gate", null, null, forgedByClient + ", " + realClient);
        assertThat(r.status())
                .as("마지막 항목(실제 접속 IP)을 기준으로 판정하므로 위조 XFF 는 통하지 않아야 한다")
                .isEqualTo(403);
    }

    // ══════════════════════════════════════════════════════════════════════
    // 캐시 즉시 리로드 · 규칙 0건 복귀
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(20)
    @DisplayName("AIPF20: 규칙을 비활성하면 활성 0건 → 즉시 전면 허용으로 복귀 (캐시 리로드)")
    void AIPF20_비활성후_전면허용복귀() {
        Resp off = call("PATCH", "/allowed-ips/" + seqRule + "/active", "{\"isActive\":false}",
                superSession, null);
        assertThat(off.status()).isEqualTo(200);
        assertThat(data(off).get("isActive").asBoolean()).isFalse();

        assertThat(call("GET", "/ip-gate", null, null, OUTSIDER).status())
                .as("CRUD 후 캐시가 즉시 갱신되지 않았다").isEqualTo(204);
    }

    @Test @Order(21)
    @DisplayName("AIPF21: 다시 활성하면 즉시 차단이 복구된다")
    void AIPF21_재활성후_차단복구() {
        Resp on = call("PATCH", "/allowed-ips/" + seqRule + "/active", "{\"isActive\":true}",
                superSession, null);
        assertThat(on.status()).isEqualTo(200);
        assertThat(call("GET", "/ip-gate", null, null, OUTSIDER).status()).isEqualTo(403);
    }

    @Test @Order(22)
    @DisplayName("AIPF22: 규칙을 삭제해도 활성 0건 → 전면 허용 (Soft Delete 반영)")
    void AIPF22_삭제후_전면허용() {
        Resp del = call("DELETE", "/allowed-ips/" + seqRule, null, superSession, null);
        assertThat(del.status()).isEqualTo(200);
        assertThat(call("GET", "/ip-gate", null, null, OUTSIDER).status()).isEqualTo(204);
    }

    @Test @Order(23)
    @DisplayName("AIPF23: 차단 시 Micrometer 카운터가 증가한다 (Prometheus 렌더명 playbook_ip_blocked_total)")
    void AIPF23_차단카운터_증가() {
        // 앞선 케이스들에서 이미 여러 번 차단됐다. 메터명은 코드상 점 표기이며
        // PrometheusNamingConvention 이 렌더링 시 playbook_ip_blocked_total 로 바꾼다.
        double total = meterRegistry.find("playbook.ip.blocked").counters().stream()
                .mapToDouble(io.micrometer.core.instrument.Counter::count)
                .sum();
        assertThat(total).as("차단 카운터가 기록되지 않았다").isGreaterThan(0d);

        boolean taggedWithOutsider = meterRegistry.find("playbook.ip.blocked").counters().stream()
                .anyMatch(c -> OUTSIDER.equals(c.getId().getTag("ip")));
        assertThat(taggedWithOutsider).as("ip 태그가 실제 차단 IP 로 기록되지 않았다").isTrue();
    }
}
