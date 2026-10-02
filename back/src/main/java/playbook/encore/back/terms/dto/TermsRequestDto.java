package playbook.encore.back.terms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermsRequestDto {

    @NotBlank(message = "약관 내용은 필수입니다.")
    private String content;
}
