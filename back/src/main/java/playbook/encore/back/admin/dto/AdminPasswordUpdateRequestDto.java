package playbook.encore.back.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminPasswordUpdateRequestDto {
    @NotBlank @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.") private String newPassword;
}
