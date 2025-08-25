package playbook.encore.back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import playbook.encore.back.data.dto.naver.NaverBookSearchRequestDto;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.data.repository.AdminRepository;
import playbook.encore.back.interceptor.LoginCheckInterceptor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class NLISBNController {

    private final AdminRepository adminRepository;
    @Value("${CLIENT_ID}")
    private String clientId;

    @Value("${CLIENT_SECRET}")
    private String clientSecret;

    @Value("${NL_API_KEY}")
    private String apiKey;

    @Value("${WORK24_API_KEY}")
    private String work24ApiKey;

    public NLISBNController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @PostMapping("/naver/book-search")
    public ResponseEntity<?> searchBook(
            HttpServletRequest request,
            @RequestBody NaverBookSearchRequestDto requestM) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                if (adminRepository.findByIdAdmin(user.getIdAdmin()).isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("관리자 정보가 없습니다.");
                }
                // ISBN으로 상세 검색 API 호출 (XML)
                String url = String.format(
                        "https://openapi.naver.com/v1/search/book_adv.xml?d_isbn=%s&display=%d",
                        URLEncoder.encode(requestM.getIsbn(), "UTF-8"),
                        requestM.getDisplay()
                );

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Naver-Client-Id", clientId);
                headers.set("X-Naver-Client-Secret", clientSecret);
                headers.set("Accept", "application/xml");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> xmlResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                // XML을 JSON으로 변환
                Map<String, Object> jsonResponse = convertXmlToJson(xmlResponse.getBody());

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonResponse);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "네이버 책 상세 검색 API 호출 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/national-library/isbn")
    public ResponseEntity<?> searchByISBN(
            HttpServletRequest request,
            @RequestBody String isbnString) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                if (adminRepository.findByIdAdmin(user.getIdAdmin()).isEmpty()) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "관리자 정보가 없습니다.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorResponse);
                }

                // ISBN 문자열 정리 (따옴표 제거, 공백 제거)
                String cleanIsbn = isbnString.replaceAll("[\"\\s-]", "").trim();

                // ISBN 유효성 검사
                if (cleanIsbn.isEmpty() || (!cleanIsbn.matches("\\d{10}") && !cleanIsbn.matches("\\d{13}"))) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "올바른 ISBN 형식이 아닙니다. (10자리 또는 13자리 숫자)");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorResponse);
                }

                // URL 생성 (long 타입으로 처리하여 13자리 ISBN 지원)
                String url = String.format(
                        "https://www.nl.go.kr/seoji/SearchApi.do?cert_key=%s&result_style=json&page_no=1&page_size=1&isbn=%s",
                        apiKey, cleanIsbn);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("User-Agent", "Mozilla/5.0 (compatible; BookManager/1.0)");

                HttpEntity<String> entity = new HttpEntity<>(headers);
                RestTemplate restTemplate = new RestTemplate();

                System.out.println("국립중앙도서관 API 요청 URL: " + url);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                String responseBody = response.getBody();

                System.out.println("국립중앙도서관 API 응답: " + responseBody);

                // 응답이 비어있는 경우 처리
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "국립중앙도서관 API에서 빈 응답을 받았습니다.");
                    errorResponse.put("docs", new ArrayList<>());
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorResponse);
                }

                // JSON 형식 검증 및 파싱
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(responseBody);

                    // 정상적인 JSON 응답인 경우 그대로 반환
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody);

                } catch (JsonProcessingException e) {
                    System.err.println("JSON 파싱 오류: " + e.getMessage());
                    System.err.println("원본 응답: " + responseBody);

                    // JSON이 아닌 응답 처리 (HTML 오류 페이지 등)
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "국립중앙도서관 API에서 올바르지 않은 형식의 응답을 받았습니다.");
                    errorResponse.put("docs", new ArrayList<>());
                    errorResponse.put("originalResponse", responseBody.substring(0, Math.min(500, responseBody.length())));

                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorResponse);
                }

            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "관리자만 접근 가능합니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }

        } catch (NumberFormatException e) {
            System.err.println("ISBN 숫자 변환 오류: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "ISBN은 숫자만 입력 가능합니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);

        } catch (HttpClientErrorException e) {
            System.err.println("HTTP 클라이언트 오류: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "외부 API 요청 중 클라이언트 오류가 발생했습니다: " + e.getStatusCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);

        } catch (HttpServerErrorException e) {
            System.err.println("HTTP 서버 오류: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "외부 API 서버에서 오류가 발생했습니다: " + e.getStatusCode());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);

        } catch (ResourceAccessException e) {
            System.err.println("네트워크 연결 오류: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "외부 API 연결 중 네트워크 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);

        } catch (Exception e) {
            System.err.println("예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버에서 예상치 못한 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @GetMapping("/work24/course")
    public ResponseEntity<?> searchWork24ByISBN() {
        try {
            String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String sixMonthAgo = LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String url = String.format(
                    "https://www.work24.go.kr/cm/openApi/call/hr/callOpenApiSvcInfo310L01.do?authKey=%s&returnType=JSON&outType=1&pageNum=1&pageSize=100&srchTraStDt=%s&srchTraEndDt=%s&srchTraArea1=11&srchNcs1=20&crseTracseSe=C0104&srchTraGbn=M1001&srchTraOrganNm=플레이데이터평생교육원&sort=ASC&sortCol=2",
                    work24ApiKey, sixMonthAgo, todayDate
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "워크24 ISBN 검색 API 호출 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Map<String, Object> convertXmlToJson(String xmlString) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

            doc.getDocumentElement().normalize();

            // channel 정보 추출
            NodeList channelList = doc.getElementsByTagName("channel");
            if (channelList.getLength() > 0) {
                Element channel = (Element) channelList.item(0);

                result.put("lastBuildDate", getElementValue(channel, "lastBuildDate"));
                result.put("total", Integer.parseInt(getElementValue(channel, "total", "0")));
                result.put("start", Integer.parseInt(getElementValue(channel, "start", "1")));
                result.put("display", Integer.parseInt(getElementValue(channel, "display", "0")));

                // item 정보 추출
                NodeList itemList = channel.getElementsByTagName("item");
                for (int i = 0; i < itemList.getLength(); i++) {
                    Element item = (Element) itemList.item(i);
                    Map<String, Object> itemMap = new HashMap<>();

                    itemMap.put("title", getElementValue(item, "title"));
                    itemMap.put("link", getElementValue(item, "link"));
                    itemMap.put("image", getElementValue(item, "image"));
                    itemMap.put("author", getElementValue(item, "author"));
                    itemMap.put("discount", getElementValue(item, "discount"));
                    itemMap.put("publisher", getElementValue(item, "publisher"));
                    itemMap.put("isbn", getElementValue(item, "isbn"));
                    itemMap.put("description", getElementValue(item, "description"));
                    itemMap.put("pubdate", getElementValue(item, "pubdate"));

                    items.add(itemMap);
                }
            }

            result.put("items", items);

        } catch (Exception e) {
            System.err.println("XML 파싱 오류: " + e.getMessage());
            result.put("error", "XML 파싱 실패: " + e.getMessage());
            result.put("total", 0);
            result.put("start", 1);
            result.put("display", 0);
            result.put("items", new ArrayList<>());
        }

        return result;
    }

    private String getElementValue(Element parent, String tagName) {
        return getElementValue(parent, tagName, "");
    }

    private String getElementValue(Element parent, String tagName, String defaultValue) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null && node.getFirstChild() != null) {
                return node.getFirstChild().getNodeValue();
            }
        }
        return defaultValue;
    }
}
