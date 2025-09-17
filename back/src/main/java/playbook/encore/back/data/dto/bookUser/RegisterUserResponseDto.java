package playbook.encore.back.data.dto.bookUser;

import org.springframework.http.HttpStatus;

import lombok.*;

@Data
public class RegisterUserResponseDto {
    private int code;
    private HttpStatus httpStatus;
    private String message;
    private Object data;

    public RegisterUserResponseDto(int code, HttpStatus httpStatus, String message, Object data) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
        this.data = data;
    }
}
