package playbook.encore.back.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class LoginAdminRequestDto {
    private int seqCampus;
    @NotBlank private String idAdmin;
    @NotBlank private String pwAdmin;

    public LoginAdminRequestDto(int seqCampus, String idAdmin, String pwAdmin) {
        this.seqCampus = seqCampus;
        this.idAdmin = idAdmin;
        this.pwAdmin = pwAdmin;
    }
}
