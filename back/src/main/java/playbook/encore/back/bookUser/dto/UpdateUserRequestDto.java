package playbook.encore.back.bookUser.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {
    @Size(max = 20, message = "이름은 20자 이하여야 합니다.") private String nameUser;
    @Size(max = 30, message = "디스코드 ID는 30자 이하여야 합니다.") private String dcUser;
}
