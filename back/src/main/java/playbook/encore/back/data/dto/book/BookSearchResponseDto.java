package playbook.encore.back.data.dto.book;

import lombok.*;

@Data
public class BookSearchResponseDto {
    private String titleBook;

    public BookSearchResponseDto(String titleBook) {
        this.titleBook = titleBook;
    }
}
