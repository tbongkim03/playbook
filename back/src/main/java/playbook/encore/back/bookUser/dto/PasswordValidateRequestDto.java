package playbook.encore.back.bookUser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordValidateRequestDto {
    @NotBlank private String password;
}
