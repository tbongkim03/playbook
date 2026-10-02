package playbook.encore.back.bookUser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class LoginUserRequestDto {
    @NotBlank private String idUser;
    @NotBlank private String pwUser;

    public LoginUserRequestDto(String idUser, String pwUser) {
        this.idUser = idUser;
        this.pwUser = pwUser;
    }
}
