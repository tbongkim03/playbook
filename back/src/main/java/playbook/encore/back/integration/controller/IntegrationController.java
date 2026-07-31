package playbook.encore.back.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.common.util.AuthUtil;
import playbook.encore.back.course.service.CourseSyncService;
import playbook.encore.back.course.service.Work24CourseClient;
import playbook.encore.back.discord.DiscordBotManager;
import playbook.encore.back.integration.dto.CampusChannelUpdateRequestDto;
import playbook.encore.back.integration.dto.IntegrationConfigUpdateRequestDto;
import playbook.encore.back.integration.dto.IntegrationTestResultDto;
import playbook.encore.back.integration.service.IntegrationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 운영자 연동 관리 (전체관리자 전용).
 * 디스코드 채널/역할 매핑, 봇 토큰·외부 API 키 CRUD 및 연동 테스트.
 */
@RestController
@RequestMapping("/integration")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;
    private final DiscordBotManager discordBotManager;
    private final Work24CourseClient work24CourseClient;
    private final CourseSyncService courseSyncService;

    // ===== 설정 CRUD =====
    @GetMapping("/configs")
    public ResponseEntity<Response> getConfigs(HttpServletRequest request) {
        AuthUtil.requireSuperAdmin(request);
        return ResponseEntity.ok(ResponseHandler.success(integrationService.getAllConfigs()));
    }

    @PutMapping("/configs/{key}")
    public ResponseEntity<Response> updateConfig(
            HttpServletRequest request,
            @PathVariable("key") String key,
            @RequestBody IntegrationConfigUpdateRequestDto body) {
        AuthUtil.requireSuperAdmin(request);
        try {
            return ResponseEntity.ok(ResponseHandler.success(
                    integrationService.updateConfig(key, body.getConfigValue())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseHandler.noData());
        }
    }

    // ===== 캠퍼스 채널/역할 매핑 =====
    @GetMapping("/campus-channels")
    public ResponseEntity<Response> getCampusChannels(HttpServletRequest request) {
        AuthUtil.requireSuperAdmin(request);
        return ResponseEntity.ok(ResponseHandler.success(integrationService.getAllCampusChannels()));
    }

    @PutMapping("/campus-channels/{campusId}")
    public ResponseEntity<Response> upsertCampusChannel(
            HttpServletRequest request,
            @PathVariable("campusId") Integer campusId,
            @RequestBody CampusChannelUpdateRequestDto body) {
        AuthUtil.requireSuperAdmin(request);
        try {
            return ResponseEntity.ok(ResponseHandler.success(
                    integrationService.upsertCampusChannel(campusId, body.getDiscordChannelId(), body.getDiscordRoleId())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseHandler.noData());
        }
    }

    // ===== 디스코드: 상태 테스트 + 재연결 =====
    @PostMapping("/test/discord")
    public ResponseEntity<Response> testDiscord(HttpServletRequest request) {
        AuthUtil.requireSuperAdmin(request);

        boolean connected = discordBotManager.isConnected();
        String appId = integrationService.getDiscordApplicationId();
        String settingsUrl = (appId != null && !appId.isBlank())
                ? "https://discord.com/developers/applications/" + appId + "/bot"
                : "https://discord.com/developers/applications";

        List<Map<String, Object>> guilds = new ArrayList<>();
        if (connected) {
            JDA jda = discordBotManager.getJda();
            for (Guild guild : jda.getGuilds()) {
                List<String> perms = guild.getSelfMember().getPermissions().stream()
                        .map(Enum::name).toList();
                guilds.add(Map.of(
                        "name", guild.getName(),
                        "id", guild.getId(),
                        "permissions", perms
                ));
            }
        }

        IntegrationTestResultDto result = IntegrationTestResultDto.builder()
                .success(connected)
                .message(connected ? "디스코드 봇이 정상 연결되어 있습니다." : "디스코드 봇이 연결되어 있지 않습니다. 토큰을 확인 후 재연결하세요.")
                .detail(Map.of("guilds", guilds))
                .settingsUrl(settingsUrl)
                .build();
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PostMapping("/discord/reconnect")
    public ResponseEntity<Response> reconnectDiscord(HttpServletRequest request) {
        AuthUtil.requireSuperAdmin(request);
        discordBotManager.reconnect();
        boolean connected = discordBotManager.isConnected();
        return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                .success(connected)
                .message(connected ? "재연결 성공" : "재연결 실패 - 토큰을 확인하세요.")
                .build()));
    }

    // ===== Work24: 실제 호출 테스트 + 수동 동기화 =====
    @PostMapping("/test/work24")
    public ResponseEntity<Response> testWork24(HttpServletRequest request) {
        AuthUtil.requireSuperAdmin(request);
        try {
            JsonNode raw = work24CourseClient.fetchRaw();
            int count = raw.path("srchList").isArray() ? raw.path("srchList").size() : 0;
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(true)
                    .message("Work24 호출 성공 - 과정 " + count + "건 수신")
                    .detail(raw.path("srchList"))
                    .build()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(false)
                    .message("Work24 호출 실패: " + e.getMessage())
                    .build()));
        }
    }

    @PostMapping("/work24/sync")
    public ResponseEntity<Response> syncWork24(HttpServletRequest request) {
        AuthUtil.requireSuperAdmin(request);
        try {
            CourseSyncService.SyncResult r = courseSyncService.sync();
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(true)
                    .message(String.format("동기화 완료 - 추가 %d, 수정 %d, 삭제 %d", r.inserted(), r.updated(), r.deleted()))
                    .detail(r)
                    .build()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(false)
                    .message("동기화 실패: " + e.getMessage())
                    .build()));
        }
    }

    // ===== 국립중앙도서관(NL): 실제 호출 테스트 =====
    @PostMapping("/test/nl")
    public ResponseEntity<Response> testNl(HttpServletRequest request) {
        AuthUtil.requireSuperAdmin(request);
        String key = integrationService.getNlApiKey();
        if (key == null || key.isBlank()) {
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(false).message("국립중앙도서관 API 키가 설정되지 않았습니다.").build()));
        }
        try {
            // 표본 ISBN(9788966261208)으로 실제 호출
            String url = String.format(
                    "https://www.nl.go.kr/seoji/SearchApi.do?cert_key=%s&result_style=json&page_no=1&page_size=1&isbn=%s",
                    key, "9788966261208");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Mozilla/5.0 (compatible; BookManager/1.0)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
            boolean ok = resp.getStatusCode().is2xxSuccessful()
                    && resp.getBody() != null && !resp.getBody().isBlank();
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(ok)
                    .message(ok ? "국립중앙도서관 ISBN API 호출 성공" : "국립중앙도서관 API 응답 오류: " + resp.getStatusCode())
                    .build()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(false)
                    .message("국립중앙도서관 API 호출 실패: " + e.getMessage())
                    .build()));
        }
    }

    // ===== Naver: 실제 호출 테스트 =====
    @PostMapping("/test/naver")
    public ResponseEntity<Response> testNaver(HttpServletRequest request) {
        AuthUtil.requireSuperAdmin(request);
        String clientId = integrationService.getNaverClientId();
        String clientSecret = integrationService.getNaverClientSecret();
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(false).message("네이버 API 키가 설정되지 않았습니다.").build()));
        }
        try {
            String url = "https://openapi.naver.com/v1/search/book.json?query=%EC%9E%90%EB%B0%94&display=1";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
            boolean ok = resp.getStatusCode().is2xxSuccessful();
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(ok)
                    .message(ok ? "네이버 책 검색 API 호출 성공" : "네이버 API 응답 오류: " + resp.getStatusCode())
                    .build()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseHandler.success(IntegrationTestResultDto.builder()
                    .success(false)
                    .message("네이버 API 호출 실패: " + e.getMessage())
                    .build()));
        }
    }
}
