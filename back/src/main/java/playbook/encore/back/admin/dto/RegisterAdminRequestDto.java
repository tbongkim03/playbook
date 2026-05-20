package playbook.encore.back.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class RegisterAdminRequestDto {
    private Integer seqCampus;  // 전체 관리자의 경우 null
    @NotBlank @Size(max = 50) private String idAdmin;
    @NotBlank @Size(max = 50) private String pwAdmin;
    @NotBlank @Size(max = 50) private String nameAdmin;
    @Size(max = 100) private String dcAdmin;
    private boolean agreeTermsAdmin;
    private boolean agreeInfoAdmin;
    private boolean agreeDiscordAlarmAdmin;
}
