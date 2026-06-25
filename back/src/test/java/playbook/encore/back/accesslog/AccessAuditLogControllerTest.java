package playbook.encore.back.accesslog;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.DirtiesContext;
import playbook.encore.back.common.BaseIntegrationTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AL5/AL7 — 접속이력(access-log)·감사로그(audit-log) 조회 API 통합 테스트.
 *
 * 테스트 흐름:
 *  AL_1  로그인 성공 → 세션 확보 + 접속이력 생성
 *  AL_2  access-log 전체 조회 (관리자 200)
 *  AL_3  access-log 유저 세션 403
 *  AL_4  access-log actorType 필터
 *  AL_5  access-log result 필터
 *  AL_6  도서 등록 → BOOK_CREATE 감사로그 생성
 *  AL_7  audit-log 전체 조회 (관리자 200)
 *  AL_8  audit-log 유저 세션 403
 *  AL_9  audit-log targetType=BOOK 필터
 *  AL_10 audit-log action=BOOK_CREATE 필터
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AccessAuditLogControllerTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    private HttpHeaders adminSession;
    private HttpHeaders userSession;

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
    }

    @AfterAll
    void teardown() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_teardown.sql"));
        }
    }

    // ─── 사전: 로그인으로 세션·접속이력 확보 ─────────────────────

    @Test @Order(1)
    void AL_1_관리자_로그인_성공_접속이력_생성() {
        adminSession = loginAsAdmin("test_admin01", "Test1234!");
        assertThat(adminSession.getFirst(HttpHeaders.COOKIE)).isNotNull();

        // 로그인 실패도 한 건 생성 (FAIL 필터 테스트용)
        loginAsAdmin("test_admin01", "wrong_password");
    }

    @Test @Order(2)
    void AL_1b_유저_로그인_성공() {
        userSession = loginAsUser("test_user01", "Test1234!");
        assertThat(userSession.getFirst(HttpHeaders.COOKIE)).isNotNull();
    }

    // ─── 접속이력 조회 ────────────────────────────────────────────

    @Test @Order(3)
    void AL_2_접속이력_관리자_전체_조회_200() {
        ResponseEntity<String> res = getWithSession("/admin/access-log", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("\"content\"");
    }

    @Test @Order(4)
    void AL_3_접속이력_유저_세션_403() {
        ResponseEntity<String> res = getWithSession("/admin/access-log", userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test @Order(5)
    void AL_4_접속이력_actorType_ADMIN_필터() {
        ResponseEntity<String> res = getWithSession("/admin/access-log?actorType=ADMIN", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        // ADMIN 필터 결과에는 USER 타입이 없어야 함
        String body = res.getBody();
        assertThat(body).doesNotContain("\"actorType\":\"USER\"");
    }

    @Test @Order(6)
    void AL_5_접속이력_result_FAIL_필터() {
        ResponseEntity<String> res = getWithSession("/admin/access-log?result=FAIL", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = res.getBody();
        // FAIL 필터 결과에는 SUCCESS가 없어야 함
        assertThat(body).doesNotContain("\"result\":\"SUCCESS\"");
    }

    // ─── 감사로그 조회 ────────────────────────────────────────────

    @Test @Order(7)
    void AL_6_도서_등록으로_감사로그_생성() {
        String body = """
                {
                  "seqCampus": 1,
                  "seqSortSecond": 4,
                  "isbnBook": "9791234567890",
                  "titleBook": "TEST_감사로그도서",
                  "authorBook": "테스트저자",
                  "publisherBook": "테스트출판사",
                  "publishDateBook": "2024-01-01",
                  "imageBook": ""
                }
                """;
        ResponseEntity<String> res = postWithSession("/books", body, adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test @Order(8)
    void AL_7_감사로그_관리자_전체_조회_200() {
        ResponseEntity<String> res = getWithSession("/admin/audit-log", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("\"content\"");
        assertThat(res.getBody()).contains("BOOK_CREATE");
    }

    @Test @Order(9)
    void AL_8_감사로그_유저_세션_403() {
        ResponseEntity<String> res = getWithSession("/admin/audit-log", userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test @Order(10)
    void AL_9_감사로그_targetType_BOOK_필터() {
        ResponseEntity<String> res = getWithSession("/admin/audit-log?targetType=BOOK", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = res.getBody();
        assertThat(body).contains("\"targetType\":\"BOOK\"");
        assertThat(body).doesNotContain("\"targetType\":\"ADMIN\"");
    }

    @Test @Order(11)
    void AL_10_감사로그_action_BOOK_CREATE_필터() {
        ResponseEntity<String> res = getWithSession("/admin/audit-log?action=BOOK_CREATE", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("BOOK_CREATE");
        assertThat(res.getBody()).doesNotContain("BOOK_DELETE");
    }
}
