package playbook.encore.back.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
public class NaverBookSearchRequestDto {
    @NotBlank private String isbn;
    @Min(1) @Max(100) private int display = 10;
}
