package playbook.encore.back.data.dto.admin;

import org.springframework.http.HttpStatus;
import lombok.*;

@Data
public class RegisterAdminResponseDto {
    private int code;
    private HttpStatus httpStatus;
    private String message;
    private Object data;

    public RegisterAdminResponseDto(int code, HttpStatus httpStatus, String message, Object data) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
        this.data = data;
    }
}
