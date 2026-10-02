package playbook.encore.back.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 캠퍼스별 디스코드 채널·역할 매핑 응답 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusChannelDto {
    private Integer seqCampus;
    private String campusName;
    private String discordChannelId;
    private String discordRoleId;
}
