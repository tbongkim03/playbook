package playbook.encore.back.bookUser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class RegisterUserRequestDto {
    @NotNull private Integer seqCorse;
    @NotBlank @Size(max = 50) private String idUser;
    @NotBlank @Size(max = 50) private String pwUser;
    @NotBlank @Size(max = 50) private String nameUser;
    @Size(max = 100) private String dcUser;
    private boolean agreeTermsUser;
    private boolean agreeInfoUser;
    private boolean agreeDiscordAlarmUser;
}
