package playbook.encore.back.book.dto;

import lombok.*;

@Data
public class BookBarcodeUniqueResponseDto {
    private boolean isDuplicated;
    private String message;

    public BookBarcodeUniqueResponseDto(boolean isDuplicated, String message) {
        this.isDuplicated = isDuplicated;
        this.message = message;
    }
}
