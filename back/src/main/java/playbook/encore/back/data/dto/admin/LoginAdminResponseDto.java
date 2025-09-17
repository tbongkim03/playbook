package playbook.encore.back.data.dto.admin;

import lombok.*;

@Data
public class LoginAdminResponseDto {
    private String token;

    public LoginAdminResponseDto(String token) {
        this.token = token;
    }
}
