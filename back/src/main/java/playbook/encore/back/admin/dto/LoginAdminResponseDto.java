package playbook.encore.back.admin.dto;

import lombok.*;

@Data
public class LoginAdminResponseDto {
    private String token;

    public LoginAdminResponseDto(String token) {
        this.token = token;
    }
}
