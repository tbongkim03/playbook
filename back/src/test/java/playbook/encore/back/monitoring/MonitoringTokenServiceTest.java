package playbook.encore.back.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

/**
 * 캠퍼스 측 토큰 회전 로직 단위 테스트 (DB·컨텍스트 없이 MockRestServiceServer로 중앙 API를 흉내).
 */
class MonitoringTokenServiceTest {

    private static final String ENDPOINT = "https://playbook-monitor.com/auth/token";
    private static final String CAMPUS = "seocho";
    private static final String INIT_TOKEN = "INIT_ACCESS_TOKEN";
    private static final String BOOT_SECRET = "BOOT_SECRET_VALUE";

    @TempDir
    Path tmpDir;

    private Path tokenFile;
    private RestTemplate rest;
    private MockRestServiceServer server;
    private MonitoringTokenService service;

    @BeforeEach
    void setUp() {
        tokenFile = tmpDir.resolve("access_token");
        rest = new RestTemplate();
        server = MockRestServiceServer.createServer(rest);
        service = new MonitoringTokenService(
                rest, ENDPOINT, CAMPUS, INIT_TOKEN, BOOT_SECRET, tokenFile.toString());
    }

    private String tokenInFile() throws IOException {
        return Files.readString(tokenFile, StandardCharsets.UTF_8).trim();
    }

    private static org.springframework.test.web.client.response.DefaultResponseCreator okToken(String token) {
        return withSuccess(
                "{\"access_token\":\"" + token + "\",\"token_type\":\"Bearer\",\"expires_at\":\"2026-07-03T00:00:00Z\"}",
                MediaType.APPLICATION_JSON);
    }

    @Test
    void 부팅시_토큰파일이_없으면_초기토큰으로_시드한다() throws IOException {
        assertThat(Files.exists(tokenFile)).isFalse();
        service.seedTokenFileIfAbsent();
        assertThat(tokenInFile()).isEqualTo(INIT_TOKEN);
    }

    @Test
    void refresh_성공시_새토큰을_파일에_기록한다() throws IOException {
        service.seedTokenFileIfAbsent();   // 파일 = INIT_TOKEN
        server.expect(once(), requestTo(ENDPOINT))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer " + INIT_TOKEN))  // 파일의 현재 토큰 사용
                .andExpect(jsonPath("$.campus").value(CAMPUS))
                .andExpect(jsonPath("$.grant_type").value("refresh"))
                .andRespond(okToken("REFRESHED_1"));

        service.rotate();

        server.verify();
        assertThat(tokenInFile()).isEqualTo("REFRESHED_1");
    }

    @Test
    void refresh_실패시_bootstrap으로_복구해_파일에_기록한다() throws IOException {
        service.seedTokenFileIfAbsent();
        // 1) refresh → 401
        server.expect(once(), requestTo(ENDPOINT))
                .andExpect(header("Authorization", "Bearer " + INIT_TOKEN))
                .andExpect(jsonPath("$.grant_type").value("refresh"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        // 2) bootstrap(시크릿) → 200
        server.expect(once(), requestTo(ENDPOINT))
                .andExpect(header("Authorization", "Bearer " + BOOT_SECRET))
                .andExpect(jsonPath("$.grant_type").value("bootstrap"))
                .andRespond(okToken("BOOTSTRAPPED_2"));

        service.rotate();

        server.verify();
        assertThat(tokenInFile()).isEqualTo("BOOTSTRAPPED_2");
    }

    @Test
    void refresh와_bootstrap_모두_실패하면_기존_토큰을_유지한다() throws IOException {
        service.seedTokenFileIfAbsent();   // INIT_TOKEN
        server.expect(once(), requestTo(ENDPOINT))
                .andExpect(jsonPath("$.grant_type").value("refresh"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(once(), requestTo(ENDPOINT))
                .andExpect(jsonPath("$.grant_type").value("bootstrap"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        service.rotate();

        server.verify();
        assertThat(tokenInFile()).isEqualTo(INIT_TOKEN);   // 변경 없음
    }

    @Test
    void 회전후_다음_refresh는_파일의_최신토큰을_자격증명으로_쓴다() throws IOException {
        service.seedTokenFileIfAbsent();
        server.expect(once(), requestTo(ENDPOINT))
                .andExpect(header("Authorization", "Bearer " + INIT_TOKEN))
                .andRespond(okToken("ROUND1"));
        service.rotate();
        server.verify();
        assertThat(tokenInFile()).isEqualTo("ROUND1");

        // 두 번째 회전: 이제 현재 토큰은 파일의 ROUND1
        MockRestServiceServer server2 = MockRestServiceServer.createServer(rest);
        server2.expect(once(), requestTo(ENDPOINT))
                .andExpect(header("Authorization", "Bearer ROUND1"))
                .andRespond(okToken("ROUND2"));
        service.rotate();
        server2.verify();
        assertThat(tokenInFile()).isEqualTo("ROUND2");
    }
}
