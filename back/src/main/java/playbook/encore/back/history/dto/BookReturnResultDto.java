package playbook.encore.back.history.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 도서 반납 처리 결과.
 *
 * <p>연체 반납은 "실패"가 아니라 "안내가 필요한 성공"이다.
 * 과거에는 이 안내를 {@code IllegalArgumentException}으로 던졌는데,
 * {@code handleBookReturn()}이 {@code @Transactional(rollbackFor = Exception.class)}이라
 * 반납 기록·도서 상태·유저 상태가 전부 롤백되면서도 컨트롤러는 200을 반환하는 버그가 있었다.
 * 연체 정보를 예외 대신 이 반환값으로 전달해 트랜잭션이 정상 커밋되게 한다.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookReturnResultDto {

    /** 연체 안내가 필요한 반납인지 여부 */
    private boolean overdue;

    /** 연체일수 (반납예정일 = 대출일 + 7일 기준). 연체가 아니면 0 */
    private long overdueDays;

    /** 연체 아님 (정상 반납) */
    public static BookReturnResultDto normal() {
        return new BookReturnResultDto(false, 0L);
    }

    /** 연체 반납 */
    public static BookReturnResultDto overdue(long overdueDays) {
        return new BookReturnResultDto(true, overdueDays);
    }
}
