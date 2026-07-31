package playbook.encore.back.history;

import org.assertj.core.api.SoftAssertions;
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

        // ── 대조군: 예외가 발생하지 않는 경로에서는 반납 결과가 DB에 커밋되어야 한다 ──
        // H6(연체 반납)과 동일한 3개 항목을 같은 방식으로 확인한다.
        // 이 단정이 통과해야 "연체일 때만 롤백된다"를 입증할 수 있다.
        int user02 = seqUser("test_user02");
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(openLoanCount(user02, 9001))
                    .as("[대조군] 미연체 반납 후 tb_history.return_dt 가 채워져 열린 대출이 0건")
                    .isZero();
            s.assertThat(isBookBorrowed(9001))
                    .as("[대조군] 미연체 반납 후 tb_book(9001).is_book_borrowed = 0")
                    .isZero();
            s.assertThat(statusUser("test_user02"))
                    .as("[대조군] 미연체 반납 후 test_user02.status_user")
                    .isEqualTo("available");
        });
    }

    @Test @Order(5)
    void H2_대출_성공_관리자() {
        // H5에서 반납된 TEST_BC001을 test_admin01 (campus 1)이 대출
        ResponseEntity<String> res = borrowWithSession("TEST_BC001", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(6)
    void H6_반납_연체처리() {
        // test_user04가 TEST_BC004 반납 (58일 전 대출 → 연체 51일, 픽스처는 CURDATE() 상대)
        // 전제: 코스 9003이 활성(finish_dt_course 미래)이어야 checkOverdueException이 예외를 던진다
        // 서비스: 상태 업데이트 후 연체 IllegalArgumentException 발생 → Controller는 200 + 연체 메시지
        ResponseEntity<String> res = returnWithSession("TEST_BC004", user4Session);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("연체");
        // "연체일수:"는 연체 안내 경로(resolveOverdueResult)만 만드는 문구다.
        assertThat(res.getBody()).contains("연체일수");

        // ── 회귀 방지: 연체 반납이 DB에 실제로 반영되는가 ──
        // 과거 버그 — 연체를 IllegalArgumentException으로 알리는 바람에
        // @Transactional(rollbackFor = Exception.class)에 걸려 반납이 통째로 롤백됐는데도
        // 컨트롤러는 프록시 바깥에서 잡아 200을 반환해 사용자에겐 성공으로 보였다.
        // 실측값: 열린 대출 1 / is_book_borrowed=1 / status_user='overdue' (반납 전과 동일).
        // 연체를 반환값으로 전달하도록 고쳐 해결했다. 아래 단정이 그 회귀를 막는다.
        int user04 = seqUser("test_user04");
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(openLoanCount(user04, 9004))
                    .as("[실험군] 연체 반납 후 tb_history.return_dt 가 채워져 열린 대출이 0건 " +
                        "(1이면 롤백되어 반납이 취소된 것)")
                    .isZero();
            s.assertThat(isBookBorrowed(9004))
                    .as("[실험군] 연체 반납 후 tb_book(9004).is_book_borrowed = 0 " +
                        "(1이면 도서가 여전히 대출 중으로 남은 것)")
                    .isZero();
            s.assertThat(statusUser("test_user04"))
                    .as("[실험군] 연체 반납 후 test_user04.status_user — 서비스는 stop으로 바꾼다 " +
                        "(overdue 그대로면 롤백된 것)")
                    .isEqualTo("stop");
        });
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

    // ─── 관리자 연체 반납 ──────────────────────────────────────

    @Test @Order(17)
    void H17_반납_연체처리_관리자() {
        // H6는 일반 유저 경로만 덮는다. resolveOverdueResult()는 isAdmin일 때도 연체 안내를 내므로
        // 과거 예외 기반 구현에서는 관리자 연체 반납도 동일하게 롤백됐다.
        // H2(Order 5)에서 test_admin01이 TEST_BC001을 오늘 대출해 아직 반납하지 않은 상태다.
        // 그 대출일만 58일 전으로 되돌려 연체(51일) 상황을 만든다.
        int updated = jdbcTemplate.update(
                "UPDATE tb_history h JOIN tb_admin a ON h.seq_admin = a.seq_admin " +
                "SET h.book_dt = DATE_SUB(CURDATE(), INTERVAL 58 DAY) " +
                "WHERE a.id_admin = 'test_admin01' AND h.seq_book = 9001 AND h.return_dt IS NULL");
        assertThat(updated).as("H2에서 만들어진 관리자 열린 대출 1건을 연체로 전환").isEqualTo(1);

        ResponseEntity<String> res = returnWithSession("TEST_BC001", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("연체");
        assertThat(res.getBody()).contains("연체일수");

        // 관리자 경로도 커밋되어야 한다 (H6와 동일한 3개 항목).
        int admin01 = seqAdmin("test_admin01");
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(openAdminLoanCount(admin01, 9001))
                    .as("[관리자] 연체 반납 후 tb_history.return_dt 가 채워져 열린 대출이 0건")
                    .isZero();
            s.assertThat(isBookBorrowed(9001))
                    .as("[관리자] 연체 반납 후 tb_book(9001).is_book_borrowed = 0")
                    .isZero();
            s.assertThat(statusAdmin("test_admin01"))
                    .as("[관리자] 연체 반납 후 test_admin01.status_admin — 서비스는 stop으로 바꾼다")
                    .isEqualTo("stop");
        });
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

    // ─── DB 상태 확인 헬퍼 ────────────────────────────────────
    // 이 테스트 클래스에는 @Transactional이 없고 요청은 실제 HTTP로 나간다.
    // 따라서 아래 조회는 서버 트랜잭션과 별개인 커넥션에서 auto-commit으로 돌아
    // "커밋된 상태"만 관찰한다 (테스트 트랜잭션이 롤백을 가리는 문제 없음).

    private int seqUser(String idUser) {
        return jdbcTemplate.queryForObject(
                "SELECT seq_user FROM tb_user WHERE id_user = ?", Integer.class, idUser);
    }

    /** 반납되지 않고 열려 있는(return_dt IS NULL) 대출 건수 */
    private int openLoanCount(int seqUser, int seqBook) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_history WHERE seq_user = ? AND seq_book = ? AND return_dt IS NULL",
                Integer.class, seqUser, seqBook);
    }

    private int isBookBorrowed(int seqBook) {
        return jdbcTemplate.queryForObject(
                "SELECT is_book_borrowed FROM tb_book WHERE seq_book = ?", Integer.class, seqBook);
    }

    private String statusUser(String idUser) {
        return jdbcTemplate.queryForObject(
                "SELECT status_user FROM tb_user WHERE id_user = ?", String.class, idUser);
    }

    private int seqAdmin(String idAdmin) {
        return jdbcTemplate.queryForObject(
                "SELECT seq_admin FROM tb_admin WHERE id_admin = ?", Integer.class, idAdmin);
    }

    /** 관리자의 반납되지 않고 열려 있는(return_dt IS NULL) 대출 건수 */
    private int openAdminLoanCount(int seqAdmin, int seqBook) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_history WHERE seq_admin = ? AND seq_book = ? AND return_dt IS NULL",
                Integer.class, seqAdmin, seqBook);
    }

    private String statusAdmin(String idAdmin) {
        return jdbcTemplate.queryForObject(
                "SELECT status_admin FROM tb_admin WHERE id_admin = ?", String.class, idAdmin);
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
