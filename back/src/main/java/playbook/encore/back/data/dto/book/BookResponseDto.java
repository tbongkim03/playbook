package playbook.encore.back.data.dto.book;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponseDto {
    private int seqBook;
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
}
