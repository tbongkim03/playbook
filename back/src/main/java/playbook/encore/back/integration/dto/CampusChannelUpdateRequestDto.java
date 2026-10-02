package playbook.encore.back.integration.dto;

import lombok.Data;

@Data
public class CampusChannelUpdateRequestDto {
    private String discordChannelId;
    private String discordRoleId;
}
