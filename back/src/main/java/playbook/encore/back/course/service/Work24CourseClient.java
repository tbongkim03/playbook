package playbook.encore.back.course.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import playbook.encore.back.integration.service.IntegrationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Work24(고용24) 훈련과정 OpenAPI 호출 클라이언트.
 * 기존에 프론트(PageRegister.vue)/NLISBNController 에 흩어져 있던 호출을 일원화하고,
 * API 키는 DB(IntegrationService)에서 읽는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Work24CourseClient {

    private static final String URL_TEMPLATE =
            "https://www.work24.go.kr/cm/openApi/call/hr/callOpenApiSvcInfo310L01.do" +
            "?authKey=%s&returnType=JSON&outType=1&pageNum=1&pageSize=100" +
            "&srchTraStDt=%s&srchTraEndDt=%s&srchTraArea1=11&srchNcs1=20" +
            "&crseTracseSe=C0104&srchTraGbn=M1001&srchTraOrganNm=플레이데이터평생교육원&sort=ASC&sortCol=2";

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IntegrationService integrationService;

    /** Work24 원본 JSON 응답 (운영자 연동 테스트용) */
    public JsonNode fetchRaw() throws Exception {
        String apiKey = integrationService.getWork24Key();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Work24 API 키가 설정되지 않았습니다.");
        }
        String today = LocalDate.now().format(YMD);
        String sixMonthAgo = LocalDate.now().minusMonths(6).format(YMD);
        String url = String.format(URL_TEMPLATE, apiKey, sixMonthAgo, today);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return new ObjectMapper().readTree(response.getBody());
    }

    /** Work24 응답을 과정 동기화용 모델로 변환 */
    public List<Work24Course> fetchCourses() throws Exception {
        JsonNode root = fetchRaw();
        JsonNode srchList = root.path("srchList");
        List<Work24Course> result = new ArrayList<>();
        if (!srchList.isArray()) {
            return result;
        }
        for (JsonNode item : srchList) {
            String rawTitle = item.path("title").asText("");
            String degree = item.path("trprDegr").asText("");
            String title = rawTitle.contains(" - ") ? rawTitle.split(" - ")[0] : rawTitle;
            String nameCourse = (title + " " + degree + "기").trim();
            if (nameCourse.length() > 30) {
                nameCourse = nameCourse.substring(0, 30);
            }
            LocalDate start = parseDate(item.path("traStartDate").asText(null));
            LocalDate finish = parseDate(item.path("traEndDate").asText(null));
            if (nameCourse.isBlank() || start == null || finish == null) {
                log.warn("[Work24] 과정 스킵 (필드 부족): title={}", rawTitle);
                continue;
            }
            result.add(new Work24Course(nameCourse, start, finish));
        }
        return result;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            log.warn("[Work24] 날짜 파싱 실패: {}", raw);
            return null;
        }
    }

    /** 동기화용 과정 모델 */
    public record Work24Course(String nameCourse, LocalDate startDtCourse, LocalDate finishDtCourse) {}
}
