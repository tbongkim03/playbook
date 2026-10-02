package playbook.encore.back.allowip.exception;

import org.springframework.http.HttpStatus;
import playbook.encore.back.common.response.ResponseCode;

/**
 * 허용 IP 도메인의 업무 예외.
 * 계약서에 지정된 ResponseCode 와 사용자 노출 메시지를 그대로 실어 컨트롤러가 통일 응답으로 변환한다.
 */
public class AllowedIpException extends RuntimeException {

    private final ResponseCode responseCode;

    public AllowedIpException(ResponseCode responseCode) {
        this(responseCode, null);
    }

    public AllowedIpException(ResponseCode responseCode, String message) {
        super(message != null ? message : responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    /** 코드 대역에 맞춰 HTTP 상태를 정한다 (GlobalExceptionHandler 와 동일한 매핑 감각). */
    public HttpStatus getHttpStatus() {
        switch (responseCode) {
            case NO_DATA:
                return HttpStatus.NOT_FOUND;
            case NOT_AUTHORIZED:
                return HttpStatus.FORBIDDEN;
            case NOT_AUTHENTICATED:
                return HttpStatus.UNAUTHORIZED;
            default:
                return HttpStatus.BAD_REQUEST;
        }
    }
}
