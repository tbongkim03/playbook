package playbook.encore.back.data.dto.book;

import lombok.*;

import java.time.LocalDate;

@Data
public class BookResponseDto {

    private Integer seqBook;
    private Integer seqSortSecond;
    private String isbnBook;
    private String titleBook;
    private String authorBook;
    private String publisherBook;
    private LocalDate publishDateBook;
    private String imageBook;
    private String barcodeBook;
    private Integer cntBook;
    private boolean printCheckBook;

    public BookResponseDto(Integer seqBook, Integer seqSortSecond, String isbnBook, String titleBook, String authorBook, String publisherBook, LocalDate publishDateBook, String imageBook, String barcodeBook, Integer cntBook, boolean printCheckBook) {
        this.seqBook = seqBook;
        this.seqSortSecond = seqSortSecond;
        this.isbnBook = isbnBook;
        this.titleBook = titleBook;
        this.authorBook = authorBook;
        this.publisherBook = publisherBook;
        this.publishDateBook = publishDateBook;
        this.imageBook = imageBook;
        this.barcodeBook = barcodeBook;
        this.cntBook = cntBook;
        this.printCheckBook = printCheckBook;
    }
}
