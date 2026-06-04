package playbook.encore.back.history.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RentalHistoryDto {
    private Integer seqHistory;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private String barcodeBook;
    private String userName;
    private String userId;
    private String courseName;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private String status;
}
