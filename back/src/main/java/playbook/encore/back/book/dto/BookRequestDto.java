package playbook.encore.back.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
public class BookRequestDto {

    @NotNull private Integer seqCampus;
    @NotNull private Integer seqSortSecond;
    private String isbnBook;
    @NotBlank private String titleBook;
    @NotBlank private String authorBook;
    @NotBlank private String publisherBook;
    private LocalDate publishDateBook;
    private String imageBook;

}
