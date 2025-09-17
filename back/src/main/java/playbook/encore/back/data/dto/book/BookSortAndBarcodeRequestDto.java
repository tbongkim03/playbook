package playbook.encore.back.data.dto.book;

import lombok.*;


@Data
public class BookSortAndBarcodeRequestDto {
    
    private Integer seqBook;
    private Integer seqSortSecond;
    private String barcodeBook;
    private Integer cntBook;
    private boolean printCheckBook;

    public BookSortAndBarcodeRequestDto(Integer seqBook, Integer seqSortSecond, String barcodeBook, Integer cntBook, boolean printCheckBook) {
        this.seqBook = seqBook;
        this.seqSortSecond = seqSortSecond;
        this.barcodeBook = barcodeBook;
        this.cntBook = cntBook;
        this.printCheckBook = printCheckBook;
    }
}
