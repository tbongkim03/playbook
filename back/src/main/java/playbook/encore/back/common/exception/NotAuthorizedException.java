package playbook.encore.back.common.exception;

public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException() {
        super("권한이 없습니다.");
    }
}
