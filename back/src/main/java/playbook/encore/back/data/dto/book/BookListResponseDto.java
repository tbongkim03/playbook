package playbook.encore.back.data.dto.book;

import java.util.List;

import lombok.*;


@Data
public class BookListResponseDto {
    private List<BookResponseDto> content;
    private Integer totalCount;

    public BookListResponseDto(List<BookResponseDto> content, Integer totalCount) {
        this.content = content;
        this.totalCount = totalCount;
    }
    
}
