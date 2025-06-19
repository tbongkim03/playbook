package playbook.encore.back.data.dto.book;

import lombok.*;

@Data
public class BookCountResponseDto {
    private String isbnBook;
    private Integer cntBook;

    public BookCountResponseDto(String isbnBook, Integer cntBook) {
        this.isbnBook = isbnBook;
        this.cntBook = cntBook;
    }
}
