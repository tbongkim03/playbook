package playbook.encore.back.allowip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 허용 IP 등록·수정 요청.
 *
 * <p>seqCampus 는 "키 없음"과 "명시적 null"을 구분하지 않는다.
 * 캠퍼스 관리자 요청이면 둘 다 자기 캠퍼스로 강제되며,
 * 다른 캠퍼스 값을 명시한 경우에만 3002 로 거부된다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllowedIpRequestDto {

    @NotBlank(message = "IP 값을 입력하세요.")
    @Size(max = 64, message = "IP 값은 64자를 넘을 수 없습니다.")
    private String ipValue;

    /** null = (전체관리자) 전역 규칙 / (캠퍼스관리자) 자기 캠퍼스 */
    private Integer seqCampus;

    @Size(max = 200, message = "설명은 200자를 넘을 수 없습니다.")
    private String description;

    /** null 이면 기본 true. JSON 키를 isActive 로 고정한다. */
    @JsonProperty("isActive")
    private Boolean isActive;
}
