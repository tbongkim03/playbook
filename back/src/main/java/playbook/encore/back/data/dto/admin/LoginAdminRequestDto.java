package playbook.encore.back.data.dto.admin;

import lombok.*;

@Data
public class LoginAdminRequestDto {
    private String idAdmin;
    private String pwAdmin;

    public LoginAdminRequestDto(String idAdmin, String pwAdmin) {
        this.idAdmin = idAdmin;
        this.pwAdmin = pwAdmin;
    }
}
