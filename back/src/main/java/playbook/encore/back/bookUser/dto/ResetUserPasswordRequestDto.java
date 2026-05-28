package playbook.encore.back.bookUser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetUserPasswordRequestDto {
    @NotBlank private String idUser;
    @NotBlank private String newPassword;
}
