package playbook.encore.back.bookUser;

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
class BookUserControllerTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    private HttpHeaders adminSession;
    private HttpHeaders userSession; // test_user01

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
        adminSession = loginAsAdmin("test_admin01", "Test1234!");
        userSession = loginAsUser("test_user01", "Test1234!");
    }

    @AfterAll
    void teardown() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_teardown.sql"));
        }
    }

    // ─── 회원가입 ──────────────────────────────────────────────

    @Test @Order(1)
    void U1_유저_등록_성공() {
        String body = """
                {"seqCorse":9001,"idUser":"test_user_reg","pwUser":"Test1234!",
                 "nameUser":"등록테스트","dcUser":"test_reg_user",
                 "agreeTermsUser":true,"agreeInfoUser":true,"agreeDiscordAlarmUser":false}
                """;
        ResponseEntity<String> res = restTemplate.postForEntity(
                baseUrl() + "/users/register", jsonEntity(body), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(2)
    void U2_유저_등록_실패_중복ID() {
        String body = """
                {"seqCorse":9001,"idUser":"test_user01","pwUser":"Test1234!",
                 "nameUser":"중복테스트","dcUser":"dup_user",
                 "agreeTermsUser":true,"agreeInfoUser":true,"agreeDiscordAlarmUser":false}
                """;
        ResponseEntity<String> res = restTemplate.postForEntity(
                baseUrl() + "/users/register", jsonEntity(body), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── 로그인 ───────────────────────────────────────────────

    @Test @Order(3)
    void U3_유저_로그인_성공() {
        HttpHeaders session = loginAsUser("test_user01", "Test1234!");
        assertThat(session.getFirst(HttpHeaders.COOKIE)).isNotNull();
        userSession = session; // 중복 로그인 방지로 S1 무효화 → S2로 갱신
    }

    @Test @Order(4)
    void U4_유저_로그인_실패() {
        ResponseEntity<String> res = restTemplate.postForEntity(
                baseUrl() + "/users/login",
                loginBody("test_user01", "wrongpassword"),
                String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── 내 정보 조회 ─────────────────────────────────────────

    @Test @Order(5)
    void U5_내정보조회_성공() {
        ResponseEntity<String> res = getWithSession("/users/me", userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("test_user01");
    }

    @Test @Order(6)
    void U6_내정보조회_미인증() {
        ResponseEntity<String> res = restTemplate.getForEntity(
                baseUrl() + "/users/me", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── 정보 수정 ────────────────────────────────────────────

    @Test @Order(7)
    void U7_정보수정_성공() {
        ResponseEntity<String> res = putWithSession(
                "/users/update",
                "{\"nameUser\":\"김철수수정\",\"dcUser\":\"test_chulsoo_upd\"}",
                userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ─── 비밀번호 ─────────────────────────────────────────────

    @Test @Order(8)
    void U8_비밀번호변경_성공() {
        ResponseEntity<String> res = putWithSession(
                "/users/password",
                "{\"newPassword\":\"Test1234!\"}",
                userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(9)
    void U9_관리자_비밀번호초기화() {
        ResponseEntity<String> res = putWithSession(
                "/users/admin/reset-password",
                "{\"idUser\":\"test_user03\",\"newPassword\":\"NewPass1!\"}",
                adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ─── 유저 목록 ────────────────────────────────────────────

    @Test @Order(10)
    void U10_유저목록_조회_성공() {
        ResponseEntity<String> res = getWithSession("/users/list", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("test_user01");
    }

    @Test @Order(11)
    void U11_유저목록_조회_권한없음() {
        ResponseEntity<String> res = getWithSession("/users/list", userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── 엑셀 다운로드 ────────────────────────────────────────

    @Test @Order(12)
    void U14_엑셀_다운로드() {
        ResponseEntity<byte[]> res = restTemplate.exchange(
                baseUrl() + "/users/export",
                HttpMethod.GET,
                new HttpEntity<>(adminSession),
                byte[].class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getHeaders().getContentType().toString()).contains("spreadsheetml");
    }

    // ─── 유저 삭제 ────────────────────────────────────────────

    @Test @Order(13)
    void U12_유저_삭제_본인() {
        // test_user02: 반납 완료, 활성 대출 없음 → 본인 삭제 가능
        HttpHeaders user2Session = loginAsUser("test_user02", "Test1234!");
        ResponseEntity<String> res = deleteWithSession("/users", null, user2Session);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(14)
    void U13_유저_삭제_관리자() {
        // U1에서 등록한 test_user_reg 삭제 (활성 대출 없음)
        ResponseEntity<String> res = deleteWithSession(
                "/users",
                "{\"idUser\":\"test_user_reg\"}",
                adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ─── 헬퍼 ─────────────────────────────────────────────────

    private HttpEntity<String> loginBody(String id, String pw) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(
                String.format("{\"idUser\":\"%s\",\"pwUser\":\"%s\"}", id, pw), headers);
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
