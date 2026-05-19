package playbook.encore.back.book.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class NaverBookSearchRequestDto {
    private String isbn;
    private int display = 10;
}
