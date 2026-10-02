package playbook.encore.back.terms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TermsResponseDto {
    private Integer seqTerms;
    private String termsType;
    private String content;
    private LocalDateTime updatedAt;
}
