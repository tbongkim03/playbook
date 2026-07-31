package playbook.encore.back.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 도서 엑셀 업로드(업데이트 전용) 결과 요약.
 * - updated : 실제 갱신된 행 수
 * - skipped : 건너뛴 행 수(빈 행·권한 밖·매칭 실패 등)
 * - errors  : 행별 오류 메시지(행 번호 포함)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookImportResultDto {
    private int updated;
    private int skipped;
    private List<String> errors;
}
