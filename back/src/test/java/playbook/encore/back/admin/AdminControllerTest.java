package playbook.encore.back.admin;

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
class AdminControllerTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    private HttpHeaders adminSession;

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

    // ─── 로그인 ───────────────────────────────────────────────

    @Test @Order(1)
    void A1_관리자_로그인_성공() {
        adminSession = loginAsAdmin("test_admin01", "Test1234!");
        assertThat(adminSession.getFirst(HttpHeaders.COOKIE)).isNotNull();
    }

    @Test @Order(2)
    void A2_관리자_로그인_실패_잘못된비밀번호() {
        ResponseEntity<String> res = restTemplate.postForEntity(
                baseUrl() + "/admin/login",
                loginBody("test_admin01", "wrongpassword"),
                String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test @Order(3)
    void A3_관리자_로그인_실패_없는ID() {
        ResponseEntity<String> res = restTemplate.postForEntity(
                baseUrl() + "/admin/login",
                loginBody("not_exist_admin", "Test1234!"),
                String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── ID 중복 확인 ─────────────────────────────────────────

    @Test @Order(4)
    void A4_관리자_ID중복확인_사용가능() {
        ResponseEntity<String> res = getWithSession("/admin/register/validate?id=brand_new_id", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("false"); // flag=false → 중복 없음 → 사용 가능
    }

    @Test @Order(5)
    void A5_관리자_ID중복확인_중복() {
        ResponseEntity<String> res = getWithSession("/admin/register/validate?id=test_admin01", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("true"); // flag=true → 중복 있음
    }

    // ─── 관리자 등록 ──────────────────────────────────────────

    @Test @Order(6)
    void A6_관리자_등록_성공() {
        String body = """
                {"seqCampus":1,"idAdmin":"test_admin_new","pwAdmin":"Test1234!",
                 "nameAdmin":"신규관리자","dcAdmin":"new_admin",
                 "agreeTermsAdmin":true,"agreeInfoAdmin":true,"agreeDiscordAlarmAdmin":false}
                """;
        ResponseEntity<String> res = postWithSession("/admin/register", body, adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(7)
    void A7_관리자_등록_권한없음_유저세션() {
        HttpHeaders userSession = loginAsUser("test_user01", "Test1234!");
        String body = """
                {"seqCampus":1,"idAdmin":"test_admin_unauth","pwAdmin":"Test1234!",
                 "nameAdmin":"권한없음","dcAdmin":"no_auth",
                 "agreeTermsAdmin":true,"agreeInfoAdmin":true,"agreeDiscordAlarmAdmin":false}
                """;
        ResponseEntity<String> res = postWithSession("/admin/register", body, userSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── 내 정보 조회 ─────────────────────────────────────────

    @Test @Order(8)
    void A8_내정보조회_성공() {
        ResponseEntity<String> res = getWithSession("/admin/me", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("test_admin01");
    }

    @Test @Order(9)
    void A9_내정보조회_미인증() {
        ResponseEntity<String> res = restTemplate.getForEntity(
                baseUrl() + "/admin/me", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── 비밀번호 확인 / 변경 ─────────────────────────────────

    @Test @Order(10)
    void A10_비밀번호_확인_성공() {
        ResponseEntity<String> res = postWithSession(
                "/admin/validate?id=test_admin01",
                "{\"password\":\"Test1234!\"}",
                adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("true");
    }

    // ─── 관리자 목록 ──────────────────────────────────────────

    @Test @Order(12)
    void A12_관리자_목록조회() {
        ResponseEntity<String> res = getWithSession("/admin/list", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("test_admin01");
    }

    // ─── 엑셀 다운로드 ────────────────────────────────────────

    @Test @Order(13)
    void A13_엑셀_다운로드() {
        ResponseEntity<byte[]> res = restTemplate.exchange(
                baseUrl() + "/admin/export",
                HttpMethod.GET,
                new HttpEntity<>(adminSession),
                byte[].class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getHeaders().getContentType().toString())
                .contains("spreadsheetml");
    }

    // ─── 관리자 삭제 ──────────────────────────────────────────

    @Test @Order(14)
    void A14_관리자_삭제() {
        ResponseEntity<String> res = deleteWithSession(
                "/admin",
                "{\"idAdmin\":\"test_admin_new\"}",
                adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ─── 로그아웃 ─────────────────────────────────────────────

    @Test @Order(15)
    void A15_로그아웃_성공() {
        ResponseEntity<String> res = postWithSession("/admin/logout", "", adminSession);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> meRes = getWithSession("/admin/me", adminSession);
        assertThat(meRes.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── 헬퍼 ─────────────────────────────────────────────────

    private HttpEntity<String> loginBody(String id, String pw) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(
                String.format("{\"idAdmin\":\"%s\",\"pwAdmin\":\"%s\",\"seqCampus\":0}", id, pw),
                headers);
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
