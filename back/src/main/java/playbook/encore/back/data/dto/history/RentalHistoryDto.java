package playbook.encore.back.data.dto.history;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RentalHistoryDto {
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private String userName;
    private String userId;
    private String courseName;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private String status;
}
