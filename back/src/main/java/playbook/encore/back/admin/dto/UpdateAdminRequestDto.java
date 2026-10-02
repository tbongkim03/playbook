package playbook.encore.back.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminRequestDto {
    @NotBlank @Size(max = 30, message = "아이디는 30자 이하여야 합니다.") private String idAdmin;
    @NotBlank @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.") private String currentPassword;
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.") private String newPassword;
    @Size(max = 30, message = "디스코드 ID는 30자 이하여야 합니다.") private String newDiscord;
}
