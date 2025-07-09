package playbook.encore.back.data.dto.book;

import lombok.*;

@Data
public class BookSearchResponseDto {
    private String titleBook;
    private boolean printCheckBook;

    public BookSearchResponseDto(String titleBook, boolean printCheckBook) {
        this.titleBook = titleBook;
        this.printCheckBook = printCheckBook;
    }
}
