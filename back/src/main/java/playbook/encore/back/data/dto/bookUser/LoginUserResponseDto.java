package playbook.encore.back.data.dto.bookUser;

import lombok.*;

@Data
public class LoginUserResponseDto {
    private String token;

    public LoginUserResponseDto(String token) {
        this.token = token;
    }
}
