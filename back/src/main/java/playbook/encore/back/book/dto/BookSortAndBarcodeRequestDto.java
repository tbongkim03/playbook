package playbook.encore.back.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;


@Data
public class BookSortAndBarcodeRequestDto {

    @NotNull private Integer seqBook;
    @NotNull private Integer seqSortSecond;
    @NotBlank @Size(max = 30, message = "바코드는 30자 이하여야 합니다.") private String barcodeBook;
    @NotNull @Min(0) private Integer cntBook;
    private boolean printCheckBook;

    public BookSortAndBarcodeRequestDto(Integer seqBook, Integer seqSortSecond, String barcodeBook, Integer cntBook, boolean printCheckBook) {
        this.seqBook = seqBook;
        this.seqSortSecond = seqSortSecond;
        this.barcodeBook = barcodeBook;
        this.cntBook = cntBook;
        this.printCheckBook = printCheckBook;
    }
}
