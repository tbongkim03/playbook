package playbook.encore.back.book.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import playbook.encore.back.book.dto.KakaoBookSearchRequestDto;
import playbook.encore.back.common.util.AuthUtil;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseCode;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.interceptor.LoginCheckInterceptor;

import java.net.URI;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class NLISBNController {

    private final AdminRepository adminRepository;
    private final playbook.encore.back.course.service.Work24CourseClient work24CourseClient;
    private final playbook.encore.back.integration.service.IntegrationService integrationService;

    public NLISBNController(AdminRepository adminRepository,
                           playbook.encore.back.course.service.Work24CourseClient work24CourseClient,
                           playbook.encore.back.integration.service.IntegrationService integrationService) {
        this.adminRepository = adminRepository;
        this.work24CourseClient = work24CourseClient;
        this.integrationService = integrationService;
    }

    /**
     * ISBN 으로 카카오 책 검색을 호출해 표지 이미지를 찾는다.
     * 국립중앙도서관 결과에 표지가 없을 때 쓰는 보조 경로다 (네이버 책 검색은 2026-07-31 종료).
     */
    @PostMapping("/kakao/book-search")
    public ResponseEntity<Response> searchBookCover(
            HttpServletRequest request,
            @RequestBody @Valid KakaoBookSearchRequestDto requestM) {
        AuthUtil.requireAdmin(request);

        String apiKey = integrationService.getKakaoRestApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.ok(ResponseHandler.error(ResponseCode.FAIL_PROCESS, "카카오 API 키가 설정되지 않았습니다."));
        }

        String cleanIsbn = requestM.getIsbn().replaceAll("[\\s-]", "");
        URI uri = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v3/search/book")
                .queryParam("target", "isbn")
                .queryParam("query", cleanIsbn)
                .queryParam("size", 1)
                .build().encode().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        headers.set("Accept", "application/json");

        try {
            ResponseEntity<String> resp = new RestTemplate()
                    .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            JsonNode docs = new ObjectMapper().readTree(resp.getBody()).path("documents");
            if (!docs.isArray() || docs.isEmpty()) {
                return ResponseEntity.ok(ResponseHandler.noData());
            }
            JsonNode first = docs.get(0);
            Map<String, String> data = new HashMap<>();
            data.put("thumbnail", first.path("thumbnail").asText(""));
            data.put("title", first.path("title").asText(""));
            return ResponseEntity.ok(ResponseHandler.success(data));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseHandler.error(ResponseCode.FAIL_PROCESS, "카카오 책 검색 호출에 실패했습니다."));
        }
    }

    @PostMapping("/national-library/isbn")
    public ResponseEntity<Response> searchByISBN(
            HttpServletRequest request,
            @RequestBody String isbnString) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                if (adminRepository.findByIdAdmin(user.getIdAdmin()).isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ResponseHandler.invalidParam("관리자 정보가 없습니다."));
                }

                String cleanIsbn = isbnString.replaceAll("[\"\\s-]", "").trim();

                if (cleanIsbn.isEmpty() || (!cleanIsbn.matches("\\d{10}") && !cleanIsbn.matches("\\d{13}"))) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ResponseHandler.invalidParamPattern("ISBN"));
                }

                String nlApiKey = integrationService.getNlApiKey();
                String url = String.format(
                        "https://www.nl.go.kr/seoji/SearchApi.do?cert_key=%s&result_style=json&page_no=1&page_size=1&isbn=%s",
                        nlApiKey, cleanIsbn);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("User-Agent", "Mozilla/5.0 (compatible; BookManager/1.0)");

                HttpEntity<String> entity = new HttpEntity<>(headers);
                RestTemplate restTemplate = new RestTemplate();

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                String responseBody = response.getBody();

                if (responseBody == null || responseBody.trim().isEmpty()) {
                    return ResponseEntity.ok(ResponseHandler.noData());
                }

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return ResponseEntity.ok(ResponseHandler.success(jsonNode));
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(ResponseHandler.error(ResponseCode.FAIL_PROCESS, "국립중앙도서관 API에서 올바르지 않은 형식의 응답을 받았습니다."));
                }

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
            }

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseHandler.invalidParamType("ISBN"));

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseHandler.error(ResponseCode.FAIL_PROCESS, "외부 API 요청 중 클라이언트 오류가 발생했습니다: " + e.getStatusCode()));

        } catch (HttpServerErrorException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ResponseHandler.error(ResponseCode.FAIL_PROCESS, "외부 API 서버에서 오류가 발생했습니다: " + e.getStatusCode()));

        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ResponseHandler.error(ResponseCode.TARGET_DISABLED, "외부 API 연결 중 네트워크 오류가 발생했습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @GetMapping("/work24/course")
    public ResponseEntity<Response> searchWork24ByISBN() {
        try {
            // Work24 호출 로직은 Work24CourseClient 로 일원화 (API 키는 DB 연동 설정에서 조회)
            JsonNode jsonNode = work24CourseClient.fetchRaw();
            return ResponseEntity.ok(ResponseHandler.success(jsonNode));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

}
