package playbook.encore.back.book.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponseDto {
    private int seqBook;
    private int seqCampus;
    private String campusName;  // 캠퍼스 이름
    private int seqSortSecond;
    private String isbnBook;
    private String titleBook;
    private String authorBook;
    private String publisherBook;
    private String publishDateBook;
    private String imageBook;
    private String barcodeBook;
    private int cntBook;
    private boolean printCheckBook;
    private boolean bookBorrowed;
    @Builder.Default
    private boolean isBorrowedByMe = false;
    @Builder.Default
    private int borrowCount = 0;  // 대출 횟수
}
