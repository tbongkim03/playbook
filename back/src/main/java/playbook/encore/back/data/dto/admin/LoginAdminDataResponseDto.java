package playbook.encore.back.data.dto.admin;

import lombok.*;

@Data
public class LoginAdminDataResponseDto {
    private String idAdmin;
    private String nameAdmin;
    private String dcAdmin;

    public LoginAdminDataResponseDto(String idAdmin, String nameAdmin, String dcAdmin) {
        this.idAdmin = idAdmin;
        this.nameAdmin = nameAdmin;
        this.dcAdmin = dcAdmin;
    }
}
