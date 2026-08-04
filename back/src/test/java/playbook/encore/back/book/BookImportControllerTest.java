package playbook.encore.back.book;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import playbook.encore.back.common.BaseIntegrationTest;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 도서 엑셀 업로드 — 신규 등록(INSERT) 지원 검증.
 *
 * <p>배경: 원래 이 기능은 "도서번호 기준 기존 도서 갱신" 전용이라 초기 장서를 넣을 수 없었다.
 * 설치 마법사가 엑셀로 초기 도서 데이터를 주입할 수 있도록 신규 등록을 추가했다.
 *
 * <p>동작 규칙
 * <ul>
 *   <li>도서번호 있음 + DB 에 존재 → UPDATE</li>
 *   <li>도서번호 <b>비어 있음</b> + {@code allowInsert=true} → INSERT</li>
 *   <li>도서번호 <b>비어 있음</b> + {@code allowInsert=false}(기본) → 건너뜀</li>
 *   <li>도서번호 있음 + DB 에 없음 → 오류 (오타를 신규 등록으로 처리하면 중복이 쌓인다)</li>
 * </ul>
 *
 * <p>{@code allowInsert} 는 <b>설치 마법사 전용</b>이다. 관리자 화면은 이 값을 보내지 않아
 * 예전처럼 "기존 도서 갱신" 전용으로 동작한다 — 실제 신규 도서는 바코드 스캔으로 등록하고,
 * 웹에서 빈 행이 조용히 등록되면 중복 도서만 쌓이기 때문이다.
 *
 * 생성 데이터는 전부 {@code TEST_} 바코드를 쓴다 — teardown 이 그 기준으로 정리한다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookImportControllerTest extends BaseIntegrationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 내보내기와 동일한 열 구성 (표지URL·라벨출력 포함) */
    private static final List<String> HEADERS = List.of(
            "도서번호", "제목", "ISBN", "저자", "출판사", "출판일",
            "대분류", "중분류", "수량", "대출상태", "바코드", "표지URL", "라벨출력");

    private HttpHeaders adminSession;   // test_admin01, campus 1

    @BeforeAll
    void setup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_setup.sql"));
        }
        adminSession = loginAsAdmin("test_admin01", "Test1234!");
        assertThat(adminSession).as("[관리자 로그인 실패]").isNotNull();
    }

    @AfterAll
    void teardown() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test_data_teardown.sql"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 업로드 헬퍼
    // ══════════════════════════════════════════════════════════════

    private byte[] workbook(List<List<String>> rows) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("도서목록");
            Row head = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) {
                head.createCell(i).setCellValue(HEADERS.get(i));
            }
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                List<String> data = rows.get(r);
                for (int c = 0; c < data.size(); c++) {
                    row.createCell(c).setCellValue(data.get(c) == null ? "" : data.get(c));
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    /** 마법사 경로 — allowInsert=true */
    private JsonNode upload(byte[] xlsx, HttpHeaders session) throws Exception {
        return upload(xlsx, session, true);
    }

    /** allowInsert 를 명시해 호출한다. 관리자 화면은 false(기본값)로 동작한다. */
    private JsonNode upload(byte[] xlsx, HttpHeaders session, boolean allowInsert) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(xlsx) {
            @Override
            public String getFilename() {
                return "books.xlsx";
            }
        });
        HttpHeaders headers = new HttpHeaders(session);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<String> res = restTemplate.postForEntity(
                baseUrl() + "/books/import?allowInsert=" + allowInsert,
                new HttpEntity<>(body, headers), String.class);
        assertThat(res.getStatusCode()).as("본문: " + res.getBody()).isEqualTo(HttpStatus.OK);
        return MAPPER.readTree(res.getBody()).path("data");
    }

    /** 신규 등록 행 — 도서번호를 비워 둔다 */
    private List<String> newRow(String title, String barcode, String printed) {
        return List.of("", title, "9791100000001", "테스트저자", "테스트출판사", "2024-01-01",
                "일반", "일반", "1", "대출가능", barcode, "https://test.img/new.jpg", printed);
    }

    // ══════════════════════════════════════════════════════════════
    // 케이스
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("BI1: 도서번호가 비어 있으면 신규 등록된다")
    void BI1_빈도서번호_신규등록() throws Exception {
        JsonNode d = upload(workbook(List.of(
                newRow("TEST 신규도서 A", "TEST_IMP001", "미출력"),
                newRow("TEST 신규도서 B", "TEST_IMP002", "미출력")
        )), adminSession);

        assertThat(d.get("inserted").asInt()).isEqualTo(2);
        assertThat(d.get("updated").asInt()).isZero();
        assertThat(d.get("errors")).isEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("BI2: 라벨출력 '출력됨' 이 신규 등록에 반영된다 (실물 라벨 부착 기록)")
    void BI2_라벨출력_보존() throws Exception {
        JsonNode d = upload(workbook(List.of(
                newRow("TEST 라벨붙은도서", "TEST_IMP003", "출력됨")
        )), adminSession);
        assertThat(d.get("inserted").asInt()).isEqualTo(1);

        // 화면 조회가 아니라 DB 값을 직접 본다 — 검증 대상이 print_check_book 컬럼 자체다.
        // 이 값이 0으로 들어가면 이관 후 라벨을 전권 다시 출력하게 된다.
        Integer printed = jdbcTemplate.queryForObject(
                "SELECT print_check_book FROM tb_book WHERE barcode_book = 'TEST_IMP003'", Integer.class);
        assertThat(printed).as("'출력됨' 이 반영되지 않았다").isEqualTo(1);

        Integer campus = jdbcTemplate.queryForObject(
                "SELECT seq_campus FROM tb_book WHERE barcode_book = 'TEST_IMP003'", Integer.class);
        assertThat(campus).as("캠퍼스 관리자는 자기 캠퍼스로 등록돼야 한다").isEqualTo(1);
    }

    @Test
    @Order(3)
    @DisplayName("BI3: 대출상태가 '대출중' 이어도 신규 등록은 항상 대출가능으로 들어간다")
    void BI3_대여상태_미이관() throws Exception {
        List<String> borrowed = List.of("", "TEST 대출중도서", "9791100000009", "저자", "출판사",
                "2024-01-01", "일반", "일반", "1", "대출중", "TEST_IMP004", "https://test.img/b.jpg", "미출력");
        JsonNode d = upload(workbook(List.of(borrowed)), adminSession);
        assertThat(d.get("inserted").asInt()).isEqualTo(1);

        // 대여이력 없이 "대출중" 이면 반납이 불가능한 유령 상태가 된다. 항상 0 이어야 한다.
        Integer borrowedFlag = jdbcTemplate.queryForObject(
                "SELECT is_book_borrowed FROM tb_book WHERE barcode_book = 'TEST_IMP004'", Integer.class);
        assertThat(borrowedFlag).as("대여 상태가 이관되면 반납 불가 유령 도서가 생긴다").isZero();
    }

    @Test
    @Order(4)
    @DisplayName("BI4: 기존 도서번호는 갱신된다 (기존 동작 유지)")
    void BI4_기존번호_갱신() throws Exception {
        List<String> update = List.of("9001", "TEST 클린코드 수정본", "9791162241820", "로버트마틴",
                "인사이트", "2013-12-24", "일반", "일반", "1", "대출가능", "TEST_BC001",
                "https://test.img/1.jpg", "미출력");
        JsonNode d = upload(workbook(List.of(update)), adminSession);

        assertThat(d.get("updated").asInt()).isEqualTo(1);
        assertThat(d.get("inserted").asInt()).isZero();
    }

    @Test
    @Order(5)
    @DisplayName("BI5: 존재하지 않는 도서번호는 오류다 (신규 등록으로 처리하지 않는다)")
    void BI5_없는번호_오류() throws Exception {
        List<String> bogus = List.of("999999", "TEST 없는번호", "9791100000002", "저자", "출판사",
                "2024-01-01", "일반", "일반", "1", "대출가능", "TEST_IMP005",
                "https://test.img/x.jpg", "미출력");
        JsonNode d = upload(workbook(List.of(bogus)), adminSession);

        assertThat(d.get("inserted").asInt()).as("오타 번호가 신규 등록되면 중복 도서가 쌓인다").isZero();
        assertThat(d.get("updated").asInt()).isZero();
        assertThat(d.get("skipped").asInt()).isEqualTo(1);
        assertThat(d.get("errors").toString()).contains("존재하지 않는 도서번호");
    }

    @Test
    @Order(6)
    @DisplayName("BI6: 신규 등록 필수 칸이 비면 그 행만 오류로 건너뛴다")
    void BI6_필수칸_누락() throws Exception {
        List<String> noTitle = List.of("", "", "9791100000003", "저자", "출판사", "2024-01-01",
                "일반", "일반", "1", "대출가능", "TEST_IMP006", "https://test.img/y.jpg", "미출력");
        JsonNode d = upload(workbook(List.of(
                noTitle,
                newRow("TEST 정상행", "TEST_IMP007", "미출력")
        )), adminSession);

        assertThat(d.get("inserted").asInt()).as("정상 행은 등록돼야 한다").isEqualTo(1);
        assertThat(d.get("skipped").asInt()).isEqualTo(1);
        assertThat(d.get("errors").toString()).contains("제목");
    }

    @Test
    @Order(7)
    @DisplayName("BI7: 없는 중분류는 오류다")
    void BI7_없는중분류() throws Exception {
        List<String> badSort = List.of("", "TEST 분류없음", "9791100000004", "저자", "출판사",
                "2024-01-01", "일반", "존재하지않는분류", "1", "대출가능", "TEST_IMP008",
                "https://test.img/z.jpg", "미출력");
        JsonNode d = upload(workbook(List.of(badSort)), adminSession);

        assertThat(d.get("inserted").asInt()).isZero();
        assertThat(d.get("errors").toString()).contains("중분류");
    }

    @Test
    @Order(8)
    @DisplayName("BI8: 완전히 빈 행은 오류 없이 건너뛴다 (엑셀 하단 잔여 행)")
    void BI8_빈행_스킵() throws Exception {
        JsonNode d = upload(workbook(List.of(
                List.of("", "", "", "", "", "", "", "", "", "", "", "", ""),
                newRow("TEST 빈행뒤정상", "TEST_IMP009", "미출력")
        )), adminSession);

        assertThat(d.get("inserted").asInt()).isEqualTo(1);
        assertThat(d.get("errors")).as("빈 행은 오류가 아니다").isEmpty();
    }

    @Test
    @Order(9)
    @DisplayName("BI9: 구 양식(표지URL·라벨출력 열 없음)으로 올려도 갱신이 동작한다")
    void BI9_구양식_호환() throws Exception {
        // 구 양식에는 두 열이 없다. 그 경우 기존 값을 건드리지 않아야 한다
        // (라벨출력을 0으로 덮으면 전권 재출력 사고가 난다).
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("도서목록");
            List<String> oldHeaders = List.of("도서번호", "제목", "ISBN", "저자", "출판사", "출판일",
                    "대분류", "중분류", "수량", "대출상태", "바코드");
            Row head = sheet.createRow(0);
            for (int i = 0; i < oldHeaders.size(); i++) head.createCell(i).setCellValue(oldHeaders.get(i));
            Row row = sheet.createRow(1);
            List<String> data = List.of("9002", "TEST 자바의정석 구양식", "9788994492032", "남궁성",
                    "EASYSPUB", "2016-01-01", "일반", "일반", "1", "대출가능", "TEST_BC002");
            for (int c = 0; c < data.size(); c++) row.createCell(c).setCellValue(data.get(c));
            wb.write(out);

            JsonNode d = upload(out.toByteArray(), adminSession);
            assertThat(d.get("updated").asInt()).isEqualTo(1);
            assertThat(d.get("errors")).isEmpty();
        }
    }

    @Test
    @Order(12)
    @DisplayName("BI12: 관리자 화면 기본값(allowInsert=false)은 빈 도서번호를 등록하지 않는다")
    void BI12_웹기본값_신규등록안함() throws Exception {
        // 실제 신규 도서는 바코드 스캔으로 등록한다. 웹 업로드에서 빈 행이 조용히 등록되면
        // 운영자가 모르는 사이 중복 도서가 쌓인다. 그래서 기본은 "갱신 전용" 이다.
        JsonNode d = upload(workbook(List.of(
                newRow("TEST 웹에서는안됨", "TEST_IMP010", "미출력")
        )), adminSession, false);

        assertThat(d.get("inserted").asInt()).as("웹 기본 경로에서 신규 등록이 일어나면 안 된다").isZero();
        assertThat(d.get("skipped").asInt()).isEqualTo(1);
        assertThat(d.get("errors")).as("건너뛴 것이지 오류가 아니다").isEmpty();

        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_book WHERE barcode_book = 'TEST_IMP010'", Integer.class);
        assertThat(cnt).isZero();
    }

    @Test
    @Order(13)
    @DisplayName("BI13: 파라미터를 아예 안 보내도 신규 등록되지 않는다 (기본값 검증)")
    void BI13_파라미터_생략시_기본값() throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(workbook(List.of(
                newRow("TEST 파라미터없음", "TEST_IMP011", "미출력")))) {
            @Override
            public String getFilename() {
                return "books.xlsx";
            }
        });
        HttpHeaders headers = new HttpHeaders(adminSession);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 쿼리스트링 없이 호출 — 관리자 화면이 실제로 이렇게 보낸다
        ResponseEntity<String> res = restTemplate.postForEntity(
                baseUrl() + "/books/import", new HttpEntity<>(body, headers), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode d = MAPPER.readTree(res.getBody()).path("data");
        assertThat(d.get("inserted").asInt()).as("기본값이 true 로 뒤집히면 안 된다").isZero();
        assertThat(d.get("skipped").asInt()).isEqualTo(1);
    }

    @Test
    @Order(11)
    @DisplayName("BI11: 실제 장서 규모(672권)를 한 번에 등록할 수 있다")
    void BI11_대량등록() throws Exception {
        // 개발 DB 의 실제 장서가 672권이다. 설치 마법사가 이 정도를 한 번에 밀어 넣는다.
        // 저자·출판사는 VARCHAR(20) 이고 실제 데이터의 최장값이 정확히 20자라 경계값으로 넣는다.
        final int COUNT = 672;
        String author20 = "가나다라마바사아자차카타파하거너더러머"; // 20자
        List<List<String>> rows = new java.util.ArrayList<>(COUNT);
        for (int i = 0; i < COUNT; i++) {
            rows.add(List.of("", "TEST 대량도서 " + i, "979110000" + String.format("%04d", i),
                    author20, author20, "2024-01-01", "일반", "일반", "1", "대출가능",
                    "TEST_BULK" + String.format("%04d", i), "https://test.img/bulk.jpg", "출력됨"));
        }

        long start = System.currentTimeMillis();
        JsonNode d = upload(workbook(rows), adminSession);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(d.get("inserted").asInt()).isEqualTo(COUNT);
        assertThat(d.get("errors")).as("경계값 20자가 길이 초과로 거부되면 안 된다").isEmpty();

        Integer saved = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_book WHERE barcode_book LIKE 'TEST_BULK%'", Integer.class);
        assertThat(saved).isEqualTo(COUNT);

        // 마법사가 이 단계에서 멈춘 것처럼 보이면 안 된다. 넉넉히 잡되 상한은 둔다.
        assertThat(elapsed).as("672권 등록에 %dms 소요 — 너무 느리면 마법사 UX 가 깨진다", elapsed)
                .isLessThan(60_000L);
    }

    @Test
    @Order(10)
    @DisplayName("BI10: 도서번호 열 자체가 없으면 파일 전체를 거부한다")
    void BI10_도서번호열_없음() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("도서목록");
            Row head = sheet.createRow(0);
            head.createCell(0).setCellValue("제목");
            head.createCell(1).setCellValue("ISBN");
            sheet.createRow(1).createCell(0).setCellValue("TEST 헤더없음");
            wb.write(out);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(out.toByteArray()) {
                @Override
                public String getFilename() {
                    return "bad.xlsx";
                }
            });
            HttpHeaders headers = new HttpHeaders(adminSession);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            ResponseEntity<String> res = restTemplate.postForEntity(
                    baseUrl() + "/books/import", new HttpEntity<>(body, headers), String.class);

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(res.getBody()).contains("도서번호");
        }
    }
}
