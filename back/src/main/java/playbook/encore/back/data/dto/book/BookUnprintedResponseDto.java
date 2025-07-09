package playbook.encore.back.data.dto.book;

import lombok.*;

@Data
public class BookUnprintedResponseDto {
    private Integer seqBook;
    private Integer seqSortSecond;
    private Integer cntBook;
    private String barcodeBook;
    private String titleBook;

    public BookUnprintedResponseDto(Integer seqBook, Integer seqSortSecond, Integer cntBook, String barcodeBook, String titleBook) {
        this.seqBook = seqBook;
        this.seqSortSecond = seqSortSecond;
        this.cntBook = cntBook;
        this.barcodeBook = barcodeBook;
        this.titleBook = titleBook;
    }
}
