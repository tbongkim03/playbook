package playbook.encore.back.admin.dto;

import lombok.*;

@Data
public class LoginAdminRequestDto {
    private int seqCampus;
    private String idAdmin;
    private String pwAdmin;

    public LoginAdminRequestDto(int seqCampus, String idAdmin, String pwAdmin) {
        this.seqCampus = seqCampus;
        this.idAdmin = idAdmin;
        this.pwAdmin = pwAdmin;
    }
}
