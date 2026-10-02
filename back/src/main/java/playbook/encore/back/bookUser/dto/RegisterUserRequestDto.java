package playbook.encore.back.bookUser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class RegisterUserRequestDto {
    @NotNull private Integer seqCorse;
    @NotBlank @Size(max = 30, message = "아이디는 30자 이하여야 합니다.") private String idUser;
    @NotBlank @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.") private String pwUser;
    @NotBlank @Size(max = 20, message = "이름은 20자 이하여야 합니다.") private String nameUser;
    @Size(max = 30, message = "디스코드 ID는 30자 이하여야 합니다.") private String dcUser;
    private boolean agreeTermsUser;
    private boolean agreeInfoUser;
    private boolean agreeDiscordAlarmUser;
}
