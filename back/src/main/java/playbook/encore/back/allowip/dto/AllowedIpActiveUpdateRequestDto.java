package playbook.encore.back.allowip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 허용 IP 활성/비활성 토글 요청. JSON 키는 isActive 로 고정한다. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllowedIpActiveUpdateRequestDto {

    @NotNull(message = "isActive 값을 지정하세요.")
    @JsonProperty("isActive")
    private Boolean isActive;
}
