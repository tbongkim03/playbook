package playbook.encore.back.data.dto.naver;

import lombok.*;

@Data
@AllArgsConstructor
public class NaverBookSearchRequestDto {
    private String query;
    private int display = 5;
    private String sort = "sim";
}
