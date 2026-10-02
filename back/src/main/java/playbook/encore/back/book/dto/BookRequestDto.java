package playbook.encore.back.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
public class BookRequestDto {

    @NotNull private Integer seqCampus;
    @NotNull private Integer seqSortSecond;
    @Size(max = 20, message = "ISBN은 20자 이하여야 합니다.") private String isbnBook;
    @NotBlank @Size(max = 255, message = "도서명은 255자 이하여야 합니다.") private String titleBook;
    @NotBlank @Size(max = 20, message = "저자명은 20자 이하여야 합니다.") private String authorBook;
    @NotBlank @Size(max = 20, message = "출판사명은 20자 이하여야 합니다.") private String publisherBook;
    private LocalDate publishDateBook;
    @Size(max = 255, message = "이미지 URL은 255자 이하여야 합니다.") private String imageBook;

}
