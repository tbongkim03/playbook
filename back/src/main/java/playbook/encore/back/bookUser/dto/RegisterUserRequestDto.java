package playbook.encore.back.bookUser.dto;

import lombok.*;

@Data
public class RegisterUserRequestDto {
    private Integer seqCorse;
    private String idUser;
    private String pwUser;
    private String nameUser;
    private String dcUser;
    private boolean agreeTermsUser;
    private boolean agreeInfoUser;
    private boolean agreeDiscordAlarmUser;
}
