package playbook.encore.back.book.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import playbook.encore.back.book.dto.NaverBookSearchRequestDto;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseCode;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.interceptor.LoginCheckInterceptor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
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
    public ResponseEntity<Response> searchBook(
            HttpServletRequest request,
            @RequestBody @Valid NaverBookSearchRequestDto requestM) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Admin user = (Admin) request.getAttribute("admin");
                if (adminRepository.findByIdAdmin(user.getIdAdmin()).isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ResponseHandler.invalidParam("관리자 정보가 없습니다."));
                }
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

                Map<String, Object> jsonResponse = convertXmlToJson(xmlResponse.getBody());
                return ResponseEntity.ok(ResponseHandler.success(jsonResponse));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
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

                String url = String.format(
                        "https://www.nl.go.kr/seoji/SearchApi.do?cert_key=%s&result_style=json&page_no=1&page_size=1&isbn=%s",
                        apiKey, cleanIsbn);

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

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return ResponseEntity.ok(ResponseHandler.success(jsonNode));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
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

            NodeList channelList = doc.getElementsByTagName("channel");
            if (channelList.getLength() > 0) {
                Element channel = (Element) channelList.item(0);

                result.put("lastBuildDate", getElementValue(channel, "lastBuildDate"));
                result.put("total", Integer.parseInt(getElementValue(channel, "total", "0")));
                result.put("start", Integer.parseInt(getElementValue(channel, "start", "1")));
                result.put("display", Integer.parseInt(getElementValue(channel, "display", "0")));

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
