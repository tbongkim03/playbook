package playbook.encore.back.common.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelUtil {

    public static Workbook createWorkbook(List<String> headers, List<List<Object>> rows) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("데이터");

        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }

        for (int r = 0; r < rows.size(); r++) {
            Row row = sheet.createRow(r + 1);
            List<Object> rowData = rows.get(r);
            for (int c = 0; c < rowData.size(); c++) {
                Cell cell = row.createCell(c);
                Object val = rowData.get(c);
                if (val == null) {
                    cell.setCellValue("-");
                } else if (val instanceof Number) {
                    cell.setCellValue(((Number) val).doubleValue());
                } else {
                    cell.setCellValue(val.toString());
                }
            }
        }

        return wb;
    }

    public static ResponseEntity<byte[]> toResponse(Workbook wb, String filename) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return toResponse(out.toByteArray(), filename);
    }

    public static ResponseEntity<byte[]> toResponse(byte[] data, String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
