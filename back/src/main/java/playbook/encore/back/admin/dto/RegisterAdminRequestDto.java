package playbook.encore.back.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class RegisterAdminRequestDto {
    private Integer seqCampus;  // 전체 관리자의 경우 null
    @NotBlank @Size(max = 30, message = "아이디는 30자 이하여야 합니다.") private String idAdmin;
    @NotBlank @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.") private String pwAdmin;
    @NotBlank @Size(max = 20, message = "이름은 20자 이하여야 합니다.") private String nameAdmin;
    @Size(max = 30, message = "디스코드 ID는 30자 이하여야 합니다.") private String dcAdmin;
    private boolean agreeTermsAdmin;
    private boolean agreeInfoAdmin;
    private boolean agreeDiscordAlarmAdmin;
}
