package playbook.encore.back.bookUser.dto;

import lombok.*;

@Data
public class RegisterIdValidateResponseDto {
    private boolean flag;

    public RegisterIdValidateResponseDto(boolean flag) {
        this.flag = flag;
    }
}
