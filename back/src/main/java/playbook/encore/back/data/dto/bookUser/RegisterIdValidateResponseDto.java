package playbook.encore.back.data.dto.bookUser;

import lombok.*;

@Data
public class RegisterIdValidateResponseDto {
    private boolean flag;

    public RegisterIdValidateResponseDto(boolean flag) {
        this.flag = flag;
    }
}
