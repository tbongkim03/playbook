package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import playbook.encore.back.data.dto.naver.NaverBookSearchRequestDto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/naver")
public class NaverSearchController {
    @Value("${CLIENT_ID}")
    private String clientId;

    @Value("${CLIENT_SECRET}")
    private String clientSecret;

    @PostMapping("/book-search")
    public ResponseEntity<?> searchBook(@RequestBody NaverBookSearchRequestDto request) {
        try {
            String encodedQuery = URLEncoder.encode(request.getQuery(), "UTF-8");
            String url = String.format(
                    "https://openapi.naver.com/v1/search/book.json?query=%s&display=%d&sort=%s",
                    encodedQuery, request.getDisplay(), request.getSort()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // JSON 문자열을 그대로 반환 (파싱 없이)
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());

        } catch (UnsupportedEncodingException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "인코딩 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "네이버 API 호출 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
