package playbook.encore.back.book.dto;

import lombok.*;

import java.time.LocalDate;

@Data
public class BookRequestDto {

    private Integer seqCampus;
    private Integer seqSortSecond;
    private String isbnBook;
    private String titleBook;
    private String authorBook;
    private String publisherBook;
    private LocalDate publishDateBook;
    private String imageBook;

}
