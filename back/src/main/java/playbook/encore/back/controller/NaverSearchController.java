package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import playbook.encore.back.data.dto.naver.NaverBookSearchRequestDto;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

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
            // ISBN으로 상세 검색 API 호출 (XML)
            String url = String.format(
                    "https://openapi.naver.com/v1/search/book_adv.xml?d_isbn=%s&display=%d",
                    URLEncoder.encode(request.getIsbn(), "UTF-8"),
                    request.getDisplay()
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

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "네이버 책 상세 검색 API 호출 실패: " + e.getMessage());
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
