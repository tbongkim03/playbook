package playbook.encore.back.data.dto.naver;

import lombok.*;

@Data
@AllArgsConstructor
public class NaverBookSearchRequestDto {
    private String isbn;
    private int display = 10;
}
