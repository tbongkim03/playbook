package playbook.encore.back.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminPasswordValidateRequestDto {
    @NotBlank private String password;
}
