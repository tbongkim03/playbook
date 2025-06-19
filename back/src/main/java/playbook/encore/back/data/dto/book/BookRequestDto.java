package playbook.encore.back.data.dto.book;

import lombok.*;

import java.time.LocalDate;

@Data
public class BookRequestDto {

    private Integer seqSortSecond;
    private String isbnBook;
    private String titleBook;
    private String authorBook;
    private String publisherBook;
    private LocalDate publishDateBook;
    private String barcodeBook;

    public BookRequestDto(Integer seqSortSecond, String isbnBook, String titleBook, String authorBook, String publisherBook, LocalDate publishDateBook, String barcodeBook) {
        this.seqSortSecond = seqSortSecond;
        this.isbnBook = isbnBook;
        this.titleBook = titleBook;
        this.authorBook = authorBook;
        this.publisherBook = publisherBook;
        this.publishDateBook = publishDateBook;
        this.barcodeBook = barcodeBook;
    }

}
