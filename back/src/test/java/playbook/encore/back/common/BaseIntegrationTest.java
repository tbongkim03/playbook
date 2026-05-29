package playbook.encore.back.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig.class)
@ContextConfiguration(initializers = EnvTestInitializer.class)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    // 관리자 로그인 후 세션 쿠키 반환
    protected HttpHeaders loginAsAdmin(String idAdmin, String pwAdmin) {
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"idAdmin\":\"%s\",\"pwAdmin\":\"%s\",\"seqCampus\":0}", idAdmin, pwAdmin);
        HttpEntity<String> request = new HttpEntity<>(body, reqHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/admin/login", request, String.class);

        return extractSessionCookie(response);
    }

    // 유저 로그인 후 세션 쿠키 반환
    protected HttpHeaders loginAsUser(String idUser, String pwUser) {
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"idUser\":\"%s\",\"pwUser\":\"%s\"}", idUser, pwUser);
        HttpEntity<String> request = new HttpEntity<>(body, reqHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/users/login", request, String.class);

        return extractSessionCookie(response);
    }

    // 인증이 필요한 GET 요청
    protected ResponseEntity<String> getWithSession(String url, HttpHeaders sessionHeaders) {
        HttpEntity<Void> request = new HttpEntity<>(sessionHeaders);
        return restTemplate.exchange(baseUrl() + url, HttpMethod.GET, request, String.class);
    }

    // 인증이 필요한 POST 요청
    protected ResponseEntity<String> postWithSession(String url, Object body, HttpHeaders sessionHeaders) {
        HttpHeaders headers = new HttpHeaders(sessionHeaders);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(baseUrl() + url, request, String.class);
    }

    // 인증이 필요한 PUT 요청
    protected ResponseEntity<String> putWithSession(String url, Object body, HttpHeaders sessionHeaders) {
        HttpHeaders headers = new HttpHeaders(sessionHeaders);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(baseUrl() + url, HttpMethod.PUT, request, String.class);
    }

    // 인증이 필요한 DELETE 요청
    protected ResponseEntity<String> deleteWithSession(String url, Object body, HttpHeaders sessionHeaders) {
        HttpHeaders headers = new HttpHeaders(sessionHeaders);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(baseUrl() + url, HttpMethod.DELETE, request, String.class);
    }

    private HttpHeaders extractSessionCookie(ResponseEntity<?> response) {
        HttpHeaders headers = new HttpHeaders();
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null && !cookies.isEmpty()) {
            String sessionCookie = cookies.stream()
                    .filter(c -> c.startsWith("SESSION"))
                    .findFirst()
                    .map(c -> c.split(";")[0])
                    .orElse(null);
            if (sessionCookie != null) {
                headers.add(HttpHeaders.COOKIE, sessionCookie);
            }
        }
        return headers;
    }
}
