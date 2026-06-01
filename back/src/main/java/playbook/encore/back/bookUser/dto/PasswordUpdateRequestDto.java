package playbook.encore.back.bookUser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequestDto {
    @NotBlank @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.") private String newPassword;
}
