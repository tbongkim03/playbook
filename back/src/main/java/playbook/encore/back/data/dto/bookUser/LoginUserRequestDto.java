package playbook.encore.back.data.dto.bookUser;

import lombok.*;

@Data
public class LoginUserRequestDto {
    private String idUser;
    private String pwUser;

    public LoginUserRequestDto(String idUser, String pwUser) {
        this.idUser = idUser;
        this.pwUser = pwUser;
    }
    
}
