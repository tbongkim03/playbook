package playbook.encore.back.bookUser.dto;

import lombok.*;

@Data
public class LoginUserResponseDto {
    private String token;

    public LoginUserResponseDto(String token) {
        this.token = token;
    }
}
