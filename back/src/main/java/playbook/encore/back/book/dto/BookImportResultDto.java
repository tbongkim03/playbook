package playbook.encore.back.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 도서 엑셀 업로드 결과 요약.
 *
 * <p>도서번호 칸으로 동작이 갈린다 (2026-08-04 신규 등록 지원 추가)
 * <ul>
 *   <li>값이 있고 DB 에 존재 → 갱신 ({@code updated})</li>
 *   <li>비어 있음 → 신규 등록 ({@code inserted})</li>
 *   <li>값이 있는데 DB 에 없음 → 오류. 오타를 조용히 신규 등록으로 처리하면 중복 도서가 쌓인다</li>
 * </ul>
 *
 * - inserted : 신규 등록된 행 수
 * - updated  : 실제 갱신된 행 수
 * - skipped  : 건너뛴 행 수(빈 행·권한 밖·매칭 실패 등)
 * - errors   : 행별 오류 메시지(행 번호 포함)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookImportResultDto {
    private int inserted;
    private int updated;
    private int skipped;
    private List<String> errors;
}
