package playbook.encore.back.book.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoBookSearchRequestDto {
    @NotBlank private String isbn;
}
