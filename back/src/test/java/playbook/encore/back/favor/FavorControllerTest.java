package playbook.encore.back.favor;

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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FavorControllerTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    private HttpHeaders adminSession;
    private HttpHeaders userSession;   // test_user01 — 즐찾 2개 (seq_book 9001, 9002)
    private HttpHeaders user4Session;  // test_user04 — 즐찾 없음

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
        adminSession = loginAsAdmin("test_admin01", "Test1234!");
        userSession = loginAsUser("test_user01", "Test1234!");
        user4Session = loginAsUser("test_user04", "Test1234!");
    }

    @AfterAll
    void teardown() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_teardown.sql"));
        }
    }

    // ─── 목록 조회 ────────────────────────────────────────────

    @Test @Order(1)
    void F1_즐겨찾기_목록조회() {
        ResponseEntity<String> res = getWithSession("/favor", userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("클린코드");
    }

    @Test @Order(2)
    void F2_즐겨찾기_목록조회_빈목록() {
        ResponseEntity<String> res = getWithSession("/favor", user4Session);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("[]");
    }

    // ─── 추가 ─────────────────────────────────────────────────

    @Test @Order(3)
    void F3_즐겨찾기_추가() {
        // test_user01에게 없는 seq_book=9004 추가
        ResponseEntity<String> res = postWithSession("/favor", 9004, userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(4)
    void F4_즐겨찾기_추가_중복() {
        // F3에서 추가한 seq_book=9004를 다시 추가 시도
        ResponseEntity<String> res = postWithSession("/favor", 9004, userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── 삭제 ─────────────────────────────────────────────────

    @Test @Order(5)
    void F5_즐겨찾기_삭제() {
        // F3에서 추가한 seq_book=9004 삭제
        ResponseEntity<String> res = deleteWithSession("/favor", 9004, userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ─── 인증 / 권한 ──────────────────────────────────────────

    @Test @Order(6)
    void F6_즐겨찾기_미인증() {
        ResponseEntity<String> res = restTemplate.getForEntity(
                baseUrl() + "/favor", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test @Order(7)
    void F7_즐겨찾기_관리자_접근불가() {
        ResponseEntity<String> res = getWithSession("/favor", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
