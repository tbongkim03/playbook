package playbook.encore.back.data.dto.admin;

import lombok.*;

@Data
public class RegisterAdminRequestDto {
    private String idAdmin;
    private String pwAdmin;
    private String nameAdmin;
    private String dcAdmin;
    private boolean agreeTermsAdmin;
    private boolean agreeInfoAdmin;
    private boolean agreeDiscordAlarmAdmin;
}
