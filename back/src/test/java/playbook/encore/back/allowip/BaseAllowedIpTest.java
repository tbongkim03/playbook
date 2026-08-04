package playbook.encore.back.allowip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import playbook.encore.back.common.BaseIntegrationTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * 허용 IP 테스트 공용 HTTP 헬퍼.
 *
 * <p>{@link BaseIntegrationTest} 의 TestRestTemplate 대신 JDK HttpClient 를 쓰는 이유는 둘이다.
 * <ol>
 *   <li><b>PATCH</b> — TestRestTemplate 의 기본 요청 팩토리(SimpleClientHttpRequestFactory)는
 *       HttpURLConnection 기반이라 PATCH 를 보낼 수 없다. 계약서 5번이 PATCH 다.</li>
 *   <li><b>X-Forwarded-For</b> — 자기차단 방지(4001/4002)와 필터 차단을 재현하려면
 *       클라이언트 IP를 가장해야 한다.</li>
 * </ol>
 *
 * <p>접속 주소를 {@code localhost} 가 아니라 <b>127.0.0.1</b> 로 고정한다.
 * localhost 가 ::1 로 해석되면 remoteAddr 이 IPv6 루프백이 되어
 * {@code playbook.ip-allowlist.trusted-proxies=127.0.0.1} 에 걸리지 않고 XFF 가 무시된다.
 */
abstract class BaseAllowedIpTest extends BaseIntegrationTest {

    protected static final ObjectMapper OM = new ObjectMapper();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /** 응답 상태·본문·Content-Type */
    protected record Resp(int status, String body, String contentType) {
    }

    protected String apiBase() {
        return "http://127.0.0.1:" + port + "/api";
    }

    /**
     * @param method  GET/POST/PUT/PATCH/DELETE
     * @param path    context-path(/api) 를 뺀 경로
     * @param body    JSON 문자열. null 이면 본문 없음
     * @param session 세션 쿠키 헤더. null 이면 비로그인
     * @param xff     X-Forwarded-For 값. null 이면 미전송
     */
    protected Resp call(String method, String path, String body, HttpHeaders session, String xff) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(apiBase() + path))
                    .timeout(Duration.ofSeconds(20));
            if (body == null) {
                b.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                b.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
                b.header("Content-Type", "application/json");
            }
            if (session != null) {
                List<String> cookies = session.get(HttpHeaders.COOKIE);
                if (cookies != null) {
                    for (String c : cookies) {
                        b.header("Cookie", c);
                    }
                }
            }
            if (xff != null) {
                b.header("X-Forwarded-For", xff);
            }
            HttpResponse<String> res = HTTP.send(b.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Resp(res.statusCode(), res.body(),
                    res.headers().firstValue("Content-Type").orElse(""));
        } catch (Exception e) {
            throw new IllegalStateException(method + " " + path + " 요청 실패", e);
        }
    }

    /** {code,msg,data} 의 data */
    protected JsonNode data(Resp r) {
        return node(r).get("data");
    }

    protected String code(Resp r) {
        JsonNode n = node(r).get("code");
        return n == null ? null : n.asText();
    }

    protected String msg(Resp r) {
        JsonNode n = node(r).get("msg");
        return n == null ? null : n.asText();
    }

    private JsonNode node(Resp r) {
        try {
            return OM.readTree(r.body());
        } catch (Exception e) {
            throw new IllegalStateException("JSON 파싱 실패 (status=" + r.status() + "): " + r.body(), e);
        }
    }
}
