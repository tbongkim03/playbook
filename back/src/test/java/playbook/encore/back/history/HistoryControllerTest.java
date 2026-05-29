package playbook.encore.back.history;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.DirtiesContext;
import playbook.encore.back.common.BaseIntegrationTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class HistoryControllerTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpHeaders adminSession;  // test_admin01, campus 1
    private HttpHeaders userSession;   // test_user02 — 미대출, available
    private HttpHeaders user1Session;  // test_user01 — 이력 2건 (반납완료 + 대출중)
    private HttpHeaders user4Session;  // test_user04 — TEST_BC004 overdue 대출 중 (campus 2)

    private int historyIdToDelete;

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
        adminSession = loginAsAdmin("test_admin01", "Test1234!");
        userSession  = loginAsUser("test_user02", "Test1234!");
        user1Session = loginAsUser("test_user01", "Test1234!");
        user4Session = loginAsUser("test_user04", "Test1234!");

        // H9 삭제 대상: test_user01의 반납 완료 이력 ID
        historyIdToDelete = jdbcTemplate.queryForObject(
                "SELECT h.seq_history FROM tb_history h " +
                "JOIN tb_user u ON h.seq_user = u.seq_user " +
                "WHERE u.id_user = 'test_user01' AND h.return_dt IS NOT NULL LIMIT 1",
                Integer.class);
    }

    @AfterAll
    void teardown() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_teardown.sql"));
        }
    }

    // ─── 대출 ────────────────────────────────────────────────────

    @Test @Order(1)
    void H1_대출_성공_유저() {
        // test_user02 (available, 미대출) → TEST_BC001 (campus 1, 빈 도서)
        ResponseEntity<String> res = borrowWithSession("TEST_BC001", userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(2)
    void H3_대출_실패_이미대출중() {
        // H1에서 대출된 TEST_BC001 재시도 → 400
        ResponseEntity<String> res = borrowWithSession("TEST_BC001", userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test @Order(3)
    void H4_대출_실패_모바일() {
        // userSession 쿠키만 추출해 완전히 새로운 headers 구성 (userSession 원본 오염 방지)
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, userSession.getFirst(HttpHeaders.COOKIE));
        headers.set(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15");
        headers.setContentType(MediaType.TEXT_PLAIN);
        ResponseEntity<String> res = restTemplate.exchange(
                baseUrl() + "/history/borrow",
                HttpMethod.POST,
                new HttpEntity<>("TEST_BC003", headers),
                String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test @Order(4)
    void H5_반납_성공() {
        // H1에서 오늘 대출한 TEST_BC001 반납 (당일 반납 — 연체 아님)
        ResponseEntity<String> res = returnWithSession("TEST_BC001", userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).doesNotContain("연체");
    }

    @Test @Order(5)
    void H2_대출_성공_관리자() {
        // H5에서 반납된 TEST_BC001을 test_admin01 (campus 1)이 대출
        ResponseEntity<String> res = borrowWithSession("TEST_BC001", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(6)
    void H6_반납_연체처리() {
        // test_user04가 TEST_BC004 반납 (2026-04-01 대출 → 연체 51일)
        // 서비스: 상태 업데이트 후 연체 IllegalArgumentException 발생 → Controller는 200 + 연체 메시지
        ResponseEntity<String> res = returnWithSession("TEST_BC004", user4Session);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("연체");
    }

    // ─── 이력 조회 ─────────────────────────────────────────────

    @Test @Order(7)
    void H7_내이력_조회() {
        // test_user01: seq_book=9001 반납완료 + seq_book=9002 대출중
        ResponseEntity<String> res = getWithSession("/history/me", user1Session);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(8)
    void H8_전체이력_조회() {
        ResponseEntity<String> res = getWithSession("/history/book", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(9)
    void H9_이력_삭제() {
        ResponseEntity<String> res = restTemplate.exchange(
                baseUrl() + "/history/book/" + historyIdToDelete,
                HttpMethod.DELETE,
                new HttpEntity<>(adminSession),
                String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ─── 통계 ─────────────────────────────────────────────────

    @Test @Order(10)
    void H10_인기대분류_전체() {
        ResponseEntity<String> res = getWithSession("/history/popular/first", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(11)
    void H11_인기대분류_코스별() {
        ResponseEntity<String> res = getWithSession("/history/popular/first/9001", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(12)
    void H12_인기중분류_전체() {
        ResponseEntity<String> res = getWithSession("/history/popular/second", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(13)
    void H13_인기중분류_코스별() {
        ResponseEntity<String> res = getWithSession("/history/popular/second/9001", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(14)
    void H14_독서왕_전체() {
        ResponseEntity<String> res = getWithSession("/history/rank", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(15)
    void H15_독서왕_코스별() {
        ResponseEntity<String> res = getWithSession("/history/rank/9001", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ─── 엑셀 다운로드 ────────────────────────────────────────

    @Test @Order(16)
    void H16_엑셀_다운로드() {
        ResponseEntity<byte[]> res = restTemplate.exchange(
                baseUrl() + "/history/export",
                HttpMethod.GET,
                new HttpEntity<>(adminSession),
                byte[].class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getHeaders().getContentType().toString()).contains("spreadsheetml");
    }

    // ─── 헬퍼 ─────────────────────────────────────────────────

    private ResponseEntity<String> borrowWithSession(String barcode, HttpHeaders sessionHeaders) {
        HttpHeaders headers = new HttpHeaders(sessionHeaders);
        headers.setContentType(MediaType.TEXT_PLAIN);
        return restTemplate.postForEntity(
                baseUrl() + "/history/borrow",
                new HttpEntity<>(barcode, headers),
                String.class);
    }

    private ResponseEntity<String> returnWithSession(String barcode, HttpHeaders sessionHeaders) {
        HttpHeaders headers = new HttpHeaders(sessionHeaders);
        headers.setContentType(MediaType.TEXT_PLAIN);
        return restTemplate.exchange(
                baseUrl() + "/history/return",
                HttpMethod.PUT,
                new HttpEntity<>(barcode, headers),
                String.class);
    }
}
