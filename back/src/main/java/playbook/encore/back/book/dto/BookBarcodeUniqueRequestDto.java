package playbook.encore.back.book.dto;

import lombok.*;

@Data
public class BookBarcodeUniqueRequestDto {
    private Integer seqBook;
    private String barcodeBook;

    public BookBarcodeUniqueRequestDto(Integer seqBook, String barcodeBook) {
        this.seqBook = seqBook;
        this.barcodeBook = barcodeBook;
    }

}
