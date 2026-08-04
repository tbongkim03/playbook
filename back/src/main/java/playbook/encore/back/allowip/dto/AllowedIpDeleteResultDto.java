package playbook.encore.back.allowip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 허용 IP 삭제(Soft Delete) 결과. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowedIpDeleteResultDto {

    private Integer seqAllowedIp;

    @JsonProperty("deleted")
    private Boolean deleted;
}
