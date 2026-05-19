package playbook.encore.back.common.response;

public enum ResponseCode {

    SUCCESS("0000", "성공"),

    NO_DATA("1001", "데이터가 없습니다"),
    EXIST_INFO("1002", "이미 존재하는 데이터입니다"),
    TIMEOUT("1003", "요청 시간이 초과되었습니다"),
    TARGET_DISABLED("1004", "대상 서버가 비활성 상태입니다"),

    INVALID_PARAM("2001", "잘못된 파라미터입니다: %s"),
    INVALID_PARAM_LEN("2002", "파라미터 길이가 잘못되었습니다: %s"),
    INVALID_PARAM_PATTERN("2003", "파라미터 패턴이 잘못되었습니다: %s"),
    INVALID_PARAM_TYPE("2004", "파라미터 타입이 잘못되었습니다: %s"),
    INVALID_PARAM_CASTING("2005", "파라미터 변환 오류입니다: %s"),

    NOT_AUTHENTICATED("3001", "인증이 필요합니다"),
    NOT_AUTHORIZED("3002", "권한이 없습니다"),
    NO_SESSION("3003", "세션 정보가 없습니다: %s"),

    FAIL_UPDATE("4001", "수정에 실패하였습니다"),
    FAIL_DELETE("4002", "삭제에 실패하였습니다"),
    FAIL_INSERT("4003", "등록에 실패하였습니다"),
    FAIL_UPSERT("4004", "등록/수정에 실패하였습니다"),
    FAIL_PROCESS("4005", "처리에 실패하였습니다"),

    UNKNOWN("9001", "알 수 없는 오류가 발생하였습니다"),
    NOT_IMPLEMENTED("9002", "구현되지 않은 기능입니다");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(String param) {
        return String.format(message, param);
    }
}
