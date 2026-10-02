package playbook.encore.back.allowip;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 허용 IP 관리 통합 테스트 (계약서 02_contract.md 1~7번).
 *
 * <p>이 클래스는 <b>차단 필터가 꺼진 상태</b>(application-test.properties 의
 * playbook.ip-allowlist.enabled=false)에서 CRUD·권한·응답 shape 을 검증한다.
 * 필터 자체의 동작(잠김 방지 fail-open, 403 평문 응답)은 {@link IpAllowlistFilterTest} 가 담당한다.
 *
 * <p><b>자기차단 방지(4001/4002) 재현 조건</b>: 요청자가 루프백이면 어차피 잠기지 않으므로
 * 서비스가 검사를 건너뛴다. application-test.properties 의
 * {@code playbook.ip-allowlist.trusted-proxies=127.0.0.1} 에 기대어
 * {@code X-Forwarded-For: 203.0.113.5} 로 클라이언트 IP를 가장해야 재현된다.
 *
 * <p>테스트용 IP는 전부 문서화 전용 대역(RFC 5737 / RFC 3849)만 쓴다 — 실데이터와 겹치지 않는다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AllowedIpControllerTest extends BaseAllowedIpTest {

    @Autowired
    private DataSource dataSource;

    private HttpHeaders superSession;   // test_admin_super — seq_campus NULL (전체관리자)
    private HttpHeaders campus1Session; // test_admin01     — seq_campus 1 (서초)
    private HttpHeaders campus2Session; // test_admin02     — seq_campus 2 (G밸리)
    private HttpHeaders userSession;    // test_user01      — 일반 사용자

    /** 케이스 간 공유하는 규칙 seq */
    private static Integer seqGlobal;      // 192.0.2.0/24    전역 (전체관리자 등록)
    private static Integer seqCampus1;     // 198.51.100.0/24 캠퍼스1 (전체관리자 등록)
    private static Integer seqCampusAdmin; // 203.0.113.10    캠퍼스1 (캠퍼스관리자 등록, seqCampus 키 부재)
    private static Integer seqCampus2;     // 198.51.100.200  캠퍼스2
    private static Integer seqSystem;      // 192.0.2.240     is_system=1 (SQL 직접 삽입)

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
        wipeAllowedIps();

        superSession = loginAsAdmin("test_admin_super", "Test1234!");
        campus1Session = loginAsAdmin("test_admin01", "Test1234!");
        campus2Session = loginAsAdmin("test_admin02", "Test1234!");
        userSession = loginAsUser("test_user01", "Test1234!");

        assertThat(superSession.get(HttpHeaders.COOKIE)).as("전체관리자 로그인 실패").isNotNull();
        assertThat(campus1Session.get(HttpHeaders.COOKIE)).as("캠퍼스관리자 로그인 실패").isNotNull();
        assertThat(userSession.get(HttpHeaders.COOKIE)).as("사용자 로그인 실패").isNotNull();
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
    // 1. 권한 매트릭스 — 비로그인 / 타권한
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(1)
    @DisplayName("AIP1: 비로그인 GET /allowed-ips → 401")
    void AIP1_목록_비로그인() {
        Resp r = call("GET", "/allowed-ips", null, null, null);
        assertThat(r.status()).isEqualTo(401);
    }

    @Test @Order(2)
    @DisplayName("AIP2: USER 세션 GET /allowed-ips → 403 / 3002")
    void AIP2_목록_사용자권한() {
        Resp r = call("GET", "/allowed-ips", null, userSession, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
    }

    @Test @Order(3)
    @DisplayName("AIP3: 비로그인 POST /allowed-ips → 401")
    void AIP3_등록_비로그인() {
        Resp r = call("POST", "/allowed-ips", "{\"ipValue\":\"192.0.2.99\"}", null, null);
        assertThat(r.status()).isEqualTo(401);
    }

    @Test @Order(4)
    @DisplayName("AIP4: USER 세션 POST /allowed-ips → 403 / 3002")
    void AIP4_등록_사용자권한() {
        Resp r = call("POST", "/allowed-ips", "{\"ipValue\":\"192.0.2.99\"}", userSession, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
    }

    @Test @Order(5)
    @DisplayName("AIP5: 비로그인 GET /allowed-ips/my-ip → 401")
    void AIP5_myIp_비로그인() {
        Resp r = call("GET", "/allowed-ips/my-ip", null, null, null);
        assertThat(r.status()).isEqualTo(401);
    }

    @Test @Order(6)
    @DisplayName("AIP6: USER 세션 GET /allowed-ips/my-ip → 403 / 3002")
    void AIP6_myIp_사용자권한() {
        Resp r = call("GET", "/allowed-ips/my-ip", null, userSession, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
    }

    @Test @Order(7)
    @DisplayName("AIP7: 비로그인 DELETE /allowed-ips/1 → 401 (변경 API도 인터셉터가 막는다)")
    void AIP7_삭제_비로그인() {
        Resp r = call("DELETE", "/allowed-ips/1", null, null, null);
        assertThat(r.status()).isEqualTo(401);
    }

    @Test @Order(8)
    @DisplayName("AIP8: USER 세션 PATCH /allowed-ips/1/active → 403 / 3002")
    void AIP8_토글_사용자권한() {
        Resp r = call("PATCH", "/allowed-ips/1/active", "{\"isActive\":false}", userSession, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
    }

    // ══════════════════════════════════════════════════════════════════════
    // 2. /ip-gate — 인증 없이 204 여야 한다 (nginx auth_request)
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(10)
    @DisplayName("AIP10: 비로그인 GET /ip-gate → 204 (인증이 붙으면 전 사용자 차단)")
    void AIP10_ipGate_비로그인_204() {
        Resp r = call("GET", "/ip-gate", null, null, null);
        assertThat(r.status()).as("/ip-gate 가 인터셉터를 타면 401 이 나온다").isEqualTo(204);
        assertThat(r.body()).isEmpty();
    }

    @Test @Order(11)
    @DisplayName("AIP11: USER 세션 GET /ip-gate → 204 (권한 게이트 없음이 의도)")
    void AIP11_ipGate_사용자세션_204() {
        Resp r = call("GET", "/ip-gate", null, userSession, null);
        assertThat(r.status()).isEqualTo(204);
    }

    @Test @Order(12)
    @DisplayName("AIP12: 관리자 세션 GET /ip-gate → 204")
    void AIP12_ipGate_관리자_204() {
        Resp r = call("GET", "/ip-gate", null, superSession, null);
        assertThat(r.status()).isEqualTo(204);
    }

    // ══════════════════════════════════════════════════════════════════════
    // 3. 등록 (POST) + 응답 shape
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(20)
    @DisplayName("AIP20: 전체관리자가 전역 규칙(seqCampus:null) 등록 → 200, seqCampus/campusName null")
    void AIP20_전역규칙_등록() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"192.0.2.0/24\",\"seqCampus\":null,\"description\":\"TEST_전역대역\",\"isActive\":true}",
                superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("seqCampus").isNull()).isTrue();
        assertThat(d.get("campusName").isNull()).isTrue();
        assertThat(d.get("ipType").asText()).isEqualTo("CIDR");
        assertThat(d.get("ipValue").asText()).isEqualTo("192.0.2.0/24");
        seqGlobal = d.get("seqAllowedIp").asInt();
    }

    @Test @Order(21)
    @DisplayName("AIP21: boolean 직렬화 규약 — isActive/isSystem/matchedByRequester 키가 그대로여야 한다")
    void AIP21_boolean_키이름() {
        Resp r = call("GET", "/allowed-ips", null, superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode row = data(r).get(0);

        assertThat(row.has("isActive")).as("isActive 키 없음 → 화면이 전부 '비활성'으로 보인다").isTrue();
        assertThat(row.has("isSystem")).isTrue();
        assertThat(row.has("matchedByRequester")).isTrue();
        assertThat(row.has("active")).as("Jackson 이 isActive 를 active 로 깎았다").isFalse();
        assertThat(row.has("system")).isFalse();
        assertThat(row.get("isActive").isBoolean()).isTrue();
        assertThat(row.get("isSystem").isBoolean()).isTrue();
        assertThat(row.get("matchedByRequester").isBoolean()).isTrue();
    }

    @Test @Order(22)
    @DisplayName("AIP22: 응답 필드 집합이 프론트(allowedIp.js·AllowedIpManagement.vue) 기대와 일치")
    void AIP22_응답_필드집합() {
        Resp r = call("GET", "/allowed-ips", null, superSession, null);
        JsonNode row = data(r).get(0);
        for (String f : List.of("seqAllowedIp", "ipValue", "ipType", "seqCampus", "campusName",
                "description", "isActive", "isSystem", "matchedByRequester", "createdAt", "updatedAt")) {
            assertThat(row.has(f)).as("프론트가 읽는 필드 누락: " + f).isTrue();
        }
    }

    @Test @Order(23)
    @DisplayName("AIP23: 전체관리자가 캠퍼스1 규칙 등록 → campusName '서초'")
    void AIP23_캠퍼스규칙_등록() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"198.51.100.0/24\",\"seqCampus\":1,\"description\":\"TEST_서초LAN\",\"isActive\":true}",
                superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("seqCampus").asInt()).isEqualTo(1);
        assertThat(d.get("campusName").asText()).isEqualTo("서초");
        seqCampus1 = d.get("seqAllowedIp").asInt();
    }

    @Test @Order(24)
    @DisplayName("AIP24: 캠퍼스관리자가 seqCampus 키를 빼고 등록 → 자기 캠퍼스로 강제 (프론트 전송 형태)")
    void AIP24_캠퍼스관리자_seqCampus_키부재() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"203.0.113.10\",\"description\":\"TEST_키부재\",\"isActive\":true}",
                campus1Session, null);
        assertThat(r.status()).as("키 부재를 3002 로 거부하면 캠퍼스 관리자가 IP를 못 넣는다").isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("seqCampus").asInt()).isEqualTo(1);
        assertThat(d.get("ipType").asText()).isEqualTo("SINGLE");
        seqCampusAdmin = d.get("seqAllowedIp").asInt();
    }

    @Test @Order(25)
    @DisplayName("AIP25: 캠퍼스관리자가 seqCampus:null 을 명시해도 자기 캠퍼스로 강제 (전역 시도로 읽지 않는다)")
    void AIP25_캠퍼스관리자_명시적null() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"203.0.113.11\",\"seqCampus\":null,\"description\":\"TEST_명시적null\"}",
                campus1Session, null);
        assertThat(r.status()).isEqualTo(200);
        assertThat(data(r).get("seqCampus").asInt()).isEqualTo(1);
    }

    @Test @Order(26)
    @DisplayName("AIP26: 캠퍼스관리자가 타 캠퍼스(2)를 지정 → 403 / 3002")
    void AIP26_캠퍼스관리자_타캠퍼스지정() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"203.0.113.12\",\"seqCampus\":2,\"description\":\"TEST_타캠퍼스\"}",
                campus1Session, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
    }

    @Test @Order(27)
    @DisplayName("AIP27: isActive 생략 시 기본 true")
    void AIP27_isActive_기본값() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"198.51.100.200\",\"seqCampus\":2,\"description\":\"TEST_캠퍼스2\"}",
                superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("isActive").asBoolean()).isTrue();
        assertThat(d.get("seqCampus").asInt()).isEqualTo(2);
        seqCampus2 = d.get("seqAllowedIp").asInt();
    }

    @Test @Order(28)
    @DisplayName("AIP28: 잘못된 IP 표기 → 400 / 2003 (msg 에 ipValue)")
    void AIP28_잘못된IP표기() {
        for (String bad : List.of("999.1.1.1", "evil.example.com", "192.0.2.0/33", "192.0.2", "1.2.3.4.5")) {
            Resp r = call("POST", "/allowed-ips",
                    "{\"ipValue\":\"" + bad + "\",\"description\":\"TEST_잘못된표기\"}", superSession, null);
            assertThat(r.status()).as("허용되면 안 되는 표기: " + bad).isEqualTo(400);
            assertThat(code(r)).as("표기 오류 코드: " + bad).isEqualTo("2003");
            assertThat(msg(r)).contains("ipValue");
        }
    }

    @Test @Order(29)
    @DisplayName("AIP29: 같은 스코프에 동일 ipValue 재등록 → 400 / 1002")
    void AIP29_중복등록() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"192.0.2.0/24\",\"seqCampus\":null,\"description\":\"TEST_중복\"}",
                superSession, null);
        assertThat(r.status()).isEqualTo(400);
        assertThat(code(r)).isEqualTo("1002");
    }

    @Test @Order(30)
    @DisplayName("AIP30: 전역과 캠퍼스는 다른 스코프 — 같은 ipValue 를 캠퍼스 규칙으로 등록 가능")
    void AIP30_스코프별_동일IP허용() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"192.0.2.0/24\",\"seqCampus\":3,\"description\":\"TEST_동작대역\"}",
                superSession, null);
        assertThat(r.status()).isEqualTo(200);
        assertThat(data(r).get("seqCampus").asInt()).isEqualTo(3);
    }

    @Test @Order(31)
    @DisplayName("AIP31: ipValue 누락 → 400 (@NotBlank)")
    void AIP31_ipValue_누락() {
        Resp r = call("POST", "/allowed-ips", "{\"description\":\"TEST_누락\"}", superSession, null);
        assertThat(r.status()).isEqualTo(400);
    }

    @Test @Order(32)
    @DisplayName("AIP32: 존재하지 않는 캠퍼스 지정 → 404 / 1001")
    void AIP32_없는캠퍼스() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"203.0.113.13\",\"seqCampus\":99999,\"description\":\"TEST_없는캠퍼스\"}",
                superSession, null);
        assertThat(r.status()).isEqualTo(404);
        assertThat(code(r)).isEqualTo("1001");
    }

    // ══════════════════════════════════════════════════════════════════════
    // 4. 목록 (GET) — 스코프·필터·정렬
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(40)
    @DisplayName("AIP40: 전체관리자 목록은 배열이고 전역 규칙이 먼저 온다")
    void AIP40_목록_정렬() {
        Resp r = call("GET", "/allowed-ips", null, superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode arr = data(r);
        assertThat(arr.isArray()).as("프론트가 res.data.data 를 배열로 쓴다").isTrue();
        assertThat(arr.size()).isGreaterThanOrEqualTo(5);
        assertThat(arr.get(0).get("seqCampus").isNull()).as("정렬: seqCampus NULL 우선").isTrue();

        Integer prev = null;
        boolean nullPhase = true;
        for (JsonNode n : arr) {
            if (n.get("seqCampus").isNull()) {
                assertThat(nullPhase).as("전역 규칙이 캠퍼스 규칙 뒤에 나왔다").isTrue();
            } else {
                nullPhase = false;
                int c = n.get("seqCampus").asInt();
                if (prev != null) {
                    assertThat(c).as("seqCampus 오름차순").isGreaterThanOrEqualTo(prev);
                }
                prev = c;
            }
        }
    }

    @Test @Order(41)
    @DisplayName("AIP41: 캠퍼스관리자 목록은 전역 + 자기 캠퍼스만 (타 캠퍼스 규칙 미노출)")
    void AIP41_목록_캠퍼스스코프() {
        Resp r = call("GET", "/allowed-ips", null, campus1Session, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode arr = data(r);
        assertThat(arr.size()).isGreaterThan(0);
        for (JsonNode n : arr) {
            if (!n.get("seqCampus").isNull()) {
                assertThat(n.get("seqCampus").asInt())
                        .as("캠퍼스 관리자에게 타 캠퍼스 규칙이 노출됐다").isEqualTo(1);
            }
        }
        assertThat(r.body()).as("전역 규칙은 조회 가능해야 한다").contains("192.0.2.0/24");
    }

    @Test @Order(42)
    @DisplayName("AIP42: 전체관리자 seqCampus=2 필터 → 캠퍼스2 규칙만")
    void AIP42_목록_캠퍼스필터() {
        Resp r = call("GET", "/allowed-ips?seqCampus=2", null, superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode arr = data(r);
        assertThat(arr.size()).isGreaterThan(0);
        for (JsonNode n : arr) {
            assertThat(n.get("seqCampus").asInt()).isEqualTo(2);
        }
    }

    @Test @Order(43)
    @DisplayName("AIP43: includeInactive=false 는 비활성 규칙을 제외한다")
    void AIP43_목록_비활성제외() {
        // 캠퍼스2 규칙을 비활성으로 만든다
        Resp off = call("PATCH", "/allowed-ips/" + seqCampus2 + "/active", "{\"isActive\":false}", superSession, null);
        assertThat(off.status()).isEqualTo(200);
        assertThat(data(off).get("isActive").asBoolean()).isFalse();

        Resp all = call("GET", "/allowed-ips?includeInactive=true", null, superSession, null);
        Resp onlyActive = call("GET", "/allowed-ips?includeInactive=false", null, superSession, null);
        assertThat(all.body()).contains("198.51.100.200");
        assertThat(onlyActive.body()).doesNotContain("198.51.100.200");

        // 원복
        call("PATCH", "/allowed-ips/" + seqCampus2 + "/active", "{\"isActive\":true}", superSession, null);
    }

    @Test @Order(44)
    @DisplayName("AIP44: 캠퍼스관리자가 보낸 seqCampus 필터는 무시된다 (에러 아님)")
    void AIP44_목록_캠퍼스관리자_필터무시() {
        Resp r = call("GET", "/allowed-ips?seqCampus=2", null, campus1Session, null);
        assertThat(r.status()).isEqualTo(200);
        for (JsonNode n : data(r)) {
            if (!n.get("seqCampus").isNull()) {
                assertThat(n.get("seqCampus").asInt()).isEqualTo(1);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // 5. 수정 (PUT) / 토글 (PATCH)
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(50)
    @DisplayName("AIP50: 전체관리자 PUT 수정 → 200, 값 반영")
    void AIP50_수정() {
        Resp r = call("PUT", "/allowed-ips/" + seqCampus1,
                "{\"ipValue\":\"198.51.100.0/25\",\"seqCampus\":1,\"description\":\"TEST_서초LAN수정\",\"isActive\":true}",
                superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("ipValue").asText()).isEqualTo("198.51.100.0/25");
        assertThat(d.get("description").asText()).isEqualTo("TEST_서초LAN수정");
    }

    @Test @Order(51)
    @DisplayName("AIP51: 캠퍼스관리자가 전역 규칙 수정 시도 → 403 / 3002 (조회만 가능)")
    void AIP51_수정_전역규칙_캠퍼스관리자() {
        Resp r = call("PUT", "/allowed-ips/" + seqGlobal,
                "{\"ipValue\":\"192.0.2.0/24\",\"description\":\"TEST_침범\"}", campus1Session, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
    }

    @Test @Order(52)
    @DisplayName("AIP52: 캠퍼스관리자가 타 캠퍼스 규칙 수정 시도 → 403 / 3002")
    void AIP52_수정_타캠퍼스_캠퍼스관리자() {
        Resp r = call("PUT", "/allowed-ips/" + seqCampus2,
                "{\"ipValue\":\"198.51.100.200\",\"description\":\"TEST_침범2\"}", campus1Session, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
    }

    @Test @Order(53)
    @DisplayName("AIP53: 없는 seqAllowedIp 수정 → 404 / 1001")
    void AIP53_수정_없는대상() {
        Resp r = call("PUT", "/allowed-ips/99999999",
                "{\"ipValue\":\"192.0.2.77\"}", superSession, null);
        assertThat(r.status()).isEqualTo(404);
        assertThat(code(r)).isEqualTo("1001");
    }

    @Test @Order(54)
    @DisplayName("AIP54: PUT 시 IP 표기 오류 → 400 / 2003")
    void AIP54_수정_잘못된표기() {
        Resp r = call("PUT", "/allowed-ips/" + seqCampus1,
                "{\"ipValue\":\"300.1.1.1\",\"seqCampus\":1}", superSession, null);
        assertThat(r.status()).isEqualTo(400);
        assertThat(code(r)).isEqualTo("2003");
    }

    @Test @Order(55)
    @DisplayName("AIP55: PATCH 활성 토글 → 200, isActive 반영")
    void AIP55_토글() {
        Resp off = call("PATCH", "/allowed-ips/" + seqCampusAdmin + "/active", "{\"isActive\":false}",
                campus1Session, null);
        assertThat(off.status()).isEqualTo(200);
        assertThat(data(off).get("isActive").asBoolean()).isFalse();

        Resp on = call("PATCH", "/allowed-ips/" + seqCampusAdmin + "/active", "{\"isActive\":true}",
                campus1Session, null);
        assertThat(on.status()).isEqualTo(200);
        assertThat(data(on).get("isActive").asBoolean()).isTrue();
    }

    @Test @Order(56)
    @DisplayName("AIP56: PATCH isActive 누락 → 400 (@NotNull)")
    void AIP56_토글_값누락() {
        Resp r = call("PATCH", "/allowed-ips/" + seqCampusAdmin + "/active", "{}", superSession, null);
        assertThat(r.status()).isEqualTo(400);
    }

    @Test @Order(57)
    @DisplayName("AIP57: 캠퍼스관리자가 전역 규칙 토글 시도 → 403 / 3002")
    void AIP57_토글_전역규칙_캠퍼스관리자() {
        Resp r = call("PATCH", "/allowed-ips/" + seqGlobal + "/active", "{\"isActive\":false}",
                campus1Session, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
    }

    // ══════════════════════════════════════════════════════════════════════
    // 6. 삭제 (DELETE) — Soft Delete · isSystem · 재등록
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(60)
    @DisplayName("AIP60: DELETE → 200, {seqAllowedIp, deleted:true}")
    void AIP60_삭제() {
        Resp r = call("DELETE", "/allowed-ips/" + seqCampusAdmin, null, campus1Session, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("seqAllowedIp").asInt()).isEqualTo(seqCampusAdmin);
        assertThat(d.has("deleted")).isTrue();
        assertThat(d.get("deleted").asBoolean()).isTrue();
    }

    @Test @Order(61)
    @DisplayName("AIP61: 삭제된 규칙은 목록에서 사라진다 (@Where use_yn='Y')")
    void AIP61_삭제후_목록미노출() {
        Resp r = call("GET", "/allowed-ips", null, superSession, null);
        assertThat(r.body()).doesNotContain("TEST_키부재");
    }

    @Test @Order(62)
    @DisplayName("AIP62: 삭제한 IP를 같은 스코프에 재등록 → 200 (Soft Delete 행 되살리기)")
    void AIP62_삭제후_재등록() {
        Resp r = call("POST", "/allowed-ips",
                "{\"ipValue\":\"203.0.113.10\",\"description\":\"TEST_재등록\",\"isActive\":true}",
                campus1Session, null);
        assertThat(r.status()).as("Duplicate entry 로 4003 이 나면 되살리기가 동작하지 않은 것").isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("ipValue").asText()).isEqualTo("203.0.113.10");
        assertThat(d.get("isActive").asBoolean()).isTrue();
        assertThat(d.get("seqCampus").asInt()).isEqualTo(1);
        assertThat(d.get("description").asText()).isEqualTo("TEST_재등록");
    }

    @Test @Order(63)
    @DisplayName("AIP63: 없는 seqAllowedIp 삭제 → 404 / 1001")
    void AIP63_삭제_없는대상() {
        Resp r = call("DELETE", "/allowed-ips/99999999", null, superSession, null);
        assertThat(r.status()).isEqualTo(404);
        assertThat(code(r)).isEqualTo("1001");
    }

    @Test @Order(64)
    @DisplayName("AIP64: isSystem 규칙 삭제 → 403 / 3002 + 계약서 메시지")
    void AIP64_삭제_시스템규칙() throws Exception {
        seqSystem = insertSystemRule("192.0.2.240", "TEST_시스템규칙");
        Resp r = call("DELETE", "/allowed-ips/" + seqSystem, null, superSession, null);
        assertThat(r.status()).isEqualTo(403);
        assertThat(code(r)).isEqualTo("3002");
        assertThat(msg(r)).isEqualTo("설치 마법사가 등록한 기본 규칙은 삭제할 수 없습니다. 비활성만 가능합니다.");
    }

    @Test @Order(65)
    @DisplayName("AIP65: isSystem 규칙도 비활성 토글은 가능하다")
    void AIP65_시스템규칙_토글가능() {
        Resp off = call("PATCH", "/allowed-ips/" + seqSystem + "/active", "{\"isActive\":false}",
                superSession, null);
        assertThat(off.status()).isEqualTo(200);
        JsonNode d = data(off);
        assertThat(d.get("isActive").asBoolean()).isFalse();
        assertThat(d.get("isSystem").asBoolean()).as("isSystem 이 응답에 true 로 나와야 프론트가 삭제 버튼을 막는다").isTrue();
    }

    @Test @Order(66)
    @DisplayName("AIP66: PUT 으로 소프트 삭제된 규칙과 같은 ipValue 를 지정 — 유니크 충돌이 어떤 코드로 나오는지 고정")
    void AIP66_수정_소프트삭제값과_충돌() {
        // 캠퍼스 스코프로 재현한다. 전역 규칙(seq_campus IS NULL)은 MySQL UNIQUE 가 NULL 중복을 허용해
        // 애초에 DB 충돌이 나지 않는다(= 이 경계 케이스가 없다).
        // 1) 규칙 A 등록 후 삭제 (use_yn='N' 행이 uk_allowed_ip_value_campus 를 계속 점유한다)
        Resp a = call("POST", "/allowed-ips",
                "{\"ipValue\":\"192.0.2.100\",\"seqCampus\":1,\"description\":\"TEST_충돌A\"}", superSession, null);
        assertThat(a.status()).isEqualTo(200);
        int seqA = data(a).get("seqAllowedIp").asInt();
        assertThat(call("DELETE", "/allowed-ips/" + seqA, null, superSession, null).status()).isEqualTo(200);

        // 2) 규칙 B 등록 후, B 의 ipValue 를 삭제된 A 의 값으로 바꾼다
        Resp b = call("POST", "/allowed-ips",
                "{\"ipValue\":\"192.0.2.101\",\"seqCampus\":1,\"description\":\"TEST_충돌B\"}", superSession, null);
        assertThat(b.status()).isEqualTo(200);
        int seqB = data(b).get("seqAllowedIp").asInt();

        Resp put = call("PUT", "/allowed-ips/" + seqB,
                "{\"ipValue\":\"192.0.2.100\",\"seqCampus\":1,\"description\":\"TEST_충돌B\"}", superSession, null);

        // 500(스택 노출)만 아니면 운영상 감당 가능하다. 실제 코드를 기록해 회귀를 잡는다.
        assertThat(put.status()).as("유니크 충돌이 500 으로 새면 안 된다").isNotEqualTo(500);
        assertThat(put.status()).isEqualTo(400);
        assertThat(code(put))
                .as("백엔드 보고서 8절은 4001 이라고 적었다 — 실제로는 커밋 시점 예외라 GlobalExceptionHandler 가 처리한다")
                .isEqualTo("2001");
    }

    @Test @Order(67)
    @DisplayName("AIP68: 전역 규칙은 MySQL UNIQUE 가 NULL 중복을 허용 — 서비스 계층 검사만이 1002 를 보장한다")
    void AIP68_전역규칙_중복은_서비스계층이_막는다() {
        Resp a = call("POST", "/allowed-ips",
                "{\"ipValue\":\"192.0.2.110\",\"seqCampus\":null,\"description\":\"TEST_전역중복A\"}", superSession, null);
        assertThat(a.status()).isEqualTo(200);
        Resp dup = call("POST", "/allowed-ips",
                "{\"ipValue\":\"192.0.2.110\",\"seqCampus\":null,\"description\":\"TEST_전역중복B\"}", superSession, null);
        assertThat(dup.status()).isEqualTo(400);
        assertThat(code(dup)).isEqualTo("1002");
        call("DELETE", "/allowed-ips/" + data(a).get("seqAllowedIp").asInt(), null, superSession, null);
    }

    @Test @Order(69)
    @DisplayName("AIP67: (정리) 충돌 검증에 쓴 규칙 제거")
    void AIP67_충돌검증_정리() {
        Resp list = call("GET", "/allowed-ips", null, superSession, null);
        for (JsonNode n : data(list)) {
            if (n.get("description").asText("").startsWith("TEST_충돌")) {
                call("DELETE", "/allowed-ips/" + n.get("seqAllowedIp").asInt(), null, superSession, null);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // 7. my-ip
    // ══════════════════════════════════════════════════════════════════════

    @Test @Order(70)
    @DisplayName("AIP70: my-ip 응답 키 — allowed/filterEnabled/bypassActive 가 깎이지 않는다")
    void AIP70_myIp_shape() {
        Resp r = call("GET", "/allowed-ips/my-ip", null, superSession, null);
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        for (String f : List.of("clientIp", "allowed", "matchedRuleSeq", "matchedRuleValue",
                "filterEnabled", "activeRuleCount", "bypassActive", "suggestedCidr")) {
            assertThat(d.has(f)).as("프론트가 읽는 필드 누락: " + f).isTrue();
        }
        assertThat(d.get("allowed").isBoolean()).isTrue();
        assertThat(d.get("filterEnabled").isBoolean()).isTrue();
        assertThat(d.get("bypassActive").isBoolean()).isTrue();
        assertThat(d.get("activeRuleCount").isInt()).isTrue();
        assertThat(d.get("filterEnabled").asBoolean()).as("test 프로파일은 차단 비활성").isFalse();
        assertThat(d.get("bypassActive").asBoolean()).isFalse();

        // test 프로파일은 trusted-proxies=127.0.0.1 이라, 로컬에서 온 요청은 신뢰 프록시로 취급되어
        // 클라이언트 IP를 XFF 에서만 읽는다. XFF 를 주고 그 값이 반영되는지로 확인한다.
        // (XFF 가 없으면 클라이언트 IP 미확정 → fail-open. AIP73 에서 따로 검증)
        Resp withXff = call("GET", "/allowed-ips/my-ip", null, superSession, "203.0.113.77");
        assertThat(data(withXff).get("clientIp").asText()).isEqualTo("203.0.113.77");
    }

    @Test @Order(71)
    @DisplayName("AIP71: X-Forwarded-For 는 마지막 항목을 클라이언트 IP로 본다 (S-1 위조 방지)")
    void AIP71_myIp_XFF반영() {
        // nginx 는 "$http_x_forwarded_for, $remote_addr" 형태로 넘길 수 있고, 그때
        // 앞쪽 항목은 클라이언트가 위조한 값이다. 신뢰할 수 있는 건 프록시가 마지막에 붙인 값뿐이다.
        Resp r = call("GET", "/allowed-ips/my-ip", null, superSession, "10.9.9.9, 203.0.113.5");
        assertThat(r.status()).isEqualTo(200);
        JsonNode d = data(r);
        assertThat(d.get("clientIp").asText())
                .as("첫 항목(10.9.9.9)이 아니라 마지막 항목이어야 한다")
                .isEqualTo("203.0.113.5");
        assertThat(d.get("suggestedCidr").asText()).isEqualTo("203.0.113.0/24");
    }

    @Test @Order(72)
    @DisplayName("AIP72: matchedByRequester 는 요청 IP 기준으로 계산된다")
    void AIP72_matchedByRequester() {
        Resp r = call("GET", "/allowed-ips", null, superSession, "203.0.113.10");
        assertThat(r.status()).isEqualTo(200);
        boolean found = false;
        for (JsonNode n : data(r)) {
            if ("203.0.113.10".equals(n.get("ipValue").asText())) {
                assertThat(n.get("matchedByRequester").asBoolean()).isTrue();
                found = true;
            } else if ("192.0.2.0/24".equals(n.get("ipValue").asText())) {
                assertThat(n.get("matchedByRequester").asBoolean()).isFalse();
            }
        }
        assertThat(found).as("203.0.113.10 규칙이 목록에 없다").isTrue();
    }

    // ══════════════════════════════════════════════════════════════════════
    // 8. 자기차단 방지 (잠김 방지 ⑨) — X-Forwarded-For 로 클라이언트 IP 가장
    // ══════════════════════════════════════════════════════════════════════

    private static final String ME = "203.0.113.5";
    private static Integer seqMine;   // 203.0.113.0/24 — 요청자를 허용하는 유일 규칙
    private static Integer seqOther;  // 198.51.100.0/24 — 요청자와 무관한 규칙

    @Test @Order(80)
    @DisplayName("AIP80: (준비) 규칙을 초기화하고 '내 IP 허용 1건 + 무관 1건' 상태를 만든다")
    void AIP80_자기차단_준비() throws Exception {
        wipeAllowedIps();
        Resp mine = call("POST", "/allowed-ips",
                "{\"ipValue\":\"203.0.113.0/24\",\"seqCampus\":null,\"description\":\"TEST_내대역\"}",
                superSession, null);
        assertThat(mine.status()).isEqualTo(200);
        seqMine = data(mine).get("seqAllowedIp").asInt();

        Resp other = call("POST", "/allowed-ips",
                "{\"ipValue\":\"198.51.100.0/24\",\"seqCampus\":null,\"description\":\"TEST_무관대역\"}",
                superSession, null);
        assertThat(other.status()).isEqualTo(200);
        seqOther = data(other).get("seqAllowedIp").asInt();
    }

    @Test @Order(81)
    @DisplayName("AIP81: 요청자를 허용하는 마지막 규칙 삭제 → 400 / 4002 + 계약서 메시지")
    void AIP81_자기차단_삭제거부() {
        Resp r = call("DELETE", "/allowed-ips/" + seqMine, null, superSession, ME);
        assertThat(r.status()).isEqualTo(400);
        assertThat(code(r)).isEqualTo("4002");
        assertThat(msg(r)).isEqualTo("현재 접속 중인 IP를 허용하는 마지막 규칙입니다.");
    }

    @Test @Order(82)
    @DisplayName("AIP82: 같은 규칙 비활성 토글 → 400 / 4001 + 계약서 메시지")
    void AIP82_자기차단_토글거부() {
        Resp r = call("PATCH", "/allowed-ips/" + seqMine + "/active", "{\"isActive\":false}", superSession, ME);
        assertThat(r.status()).isEqualTo(400);
        assertThat(code(r)).isEqualTo("4001");
        assertThat(msg(r)).isEqualTo("현재 접속 중인 IP가 차단됩니다. 먼저 다른 허용 규칙을 등록하세요.");
    }

    @Test @Order(83)
    @DisplayName("AIP83: 같은 규칙의 ipValue 를 나를 제외하는 값으로 수정 → 400 / 4001")
    void AIP83_자기차단_수정거부() {
        Resp r = call("PUT", "/allowed-ips/" + seqMine,
                "{\"ipValue\":\"192.0.2.0/24\",\"seqCampus\":null,\"description\":\"TEST_내대역\"}",
                superSession, ME);
        assertThat(r.status()).isEqualTo(400);
        assertThat(code(r)).isEqualTo("4001");
        assertThat(msg(r)).contains("현재 접속 중인 IP가 차단됩니다");
    }

    @Test @Order(84)
    @DisplayName("AIP84: 거부된 변경은 실제로 반영되지 않았다 (롤백 확인)")
    void AIP84_자기차단_거부후_상태유지() {
        Resp r = call("GET", "/allowed-ips", null, superSession, ME);
        boolean found = false;
        for (JsonNode n : data(r)) {
            if (seqMine.equals(n.get("seqAllowedIp").asInt())) {
                assertThat(n.get("ipValue").asText()).isEqualTo("203.0.113.0/24");
                assertThat(n.get("isActive").asBoolean()).isTrue();
                assertThat(n.get("matchedByRequester").asBoolean()).isTrue();
                found = true;
            }
        }
        assertThat(found).isTrue();
    }

    @Test @Order(85)
    @DisplayName("AIP85: 나를 허용하는 다른 규칙을 먼저 등록하면 삭제가 허용된다 (과차단 아님)")
    void AIP85_자기차단_대체규칙후_삭제허용() {
        Resp add = call("POST", "/allowed-ips",
                "{\"ipValue\":\"203.0.113.5\",\"seqCampus\":null,\"description\":\"TEST_내단일IP\"}",
                superSession, ME);
        assertThat(add.status()).isEqualTo(200);

        Resp del = call("DELETE", "/allowed-ips/" + seqMine, null, superSession, ME);
        assertThat(del.status()).as("대체 규칙이 있는데도 막으면 과차단이다").isEqualTo(200);
        assertThat(data(del).get("deleted").asBoolean()).isTrue();
    }

    @Test @Order(86)
    @DisplayName("AIP86: 요청자가 루프백이면 자기차단 검사를 건너뛴다 (관리실 PC 잠김 방지)")
    void AIP86_루프백은_검사스킵() {
        // XFF 없이 = 127.0.0.1. 남은 규칙(203.0.113.5, 198.51.100.0/24) 중 무엇을 지워도 통과해야 한다
        Resp r = call("DELETE", "/allowed-ips/" + seqOther, null, superSession, null);
        assertThat(r.status()).isEqualTo(200);
    }

    // ══════════════════════════════════════════════════════════════════════
    // 헬퍼
    // ══════════════════════════════════════════════════════════════════════

    /** is_system=1 규칙은 API로 만들 수 없으므로 SQL 로 직접 넣는다 (설치 마법사 시딩 상황 재현). */
    private Integer insertSystemRule(String ipValue, String description) throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("INSERT INTO tb_allowed_ip "
                    + "(ip_value, ip_type, seq_campus, description, is_active, is_system, use_yn, created_at) "
                    + "VALUES ('" + ipValue + "', 'SINGLE', NULL, '" + description + "', 1, 1, 'Y', NOW())");
            try (ResultSet rs = st.executeQuery(
                    "SELECT seq_allowed_ip FROM tb_allowed_ip WHERE ip_value = '" + ipValue + "'")) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
