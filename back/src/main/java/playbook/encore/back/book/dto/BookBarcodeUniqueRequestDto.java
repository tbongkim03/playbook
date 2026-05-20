package playbook.encore.back.book.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class BookBarcodeUniqueRequestDto {
    private Integer seqBook;
    @NotBlank private String barcodeBook;

    public BookBarcodeUniqueRequestDto(Integer seqBook, String barcodeBook) {
        this.seqBook = seqBook;
        this.barcodeBook = barcodeBook;
    }

}
