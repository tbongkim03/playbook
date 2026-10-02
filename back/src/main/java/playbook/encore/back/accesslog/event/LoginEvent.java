package playbook.encore.back.accesslog.event;

import lombok.Getter;

@Getter
public class LoginEvent {

    private final String actorType;
    private final Long actorId;
    private final String actorName;
    private final String ipAddress;
    private final String result;
    private final String failReason;

    private LoginEvent(String actorType, Long actorId, String actorName,
                       String ipAddress, String result, String failReason) {
        this.actorType = actorType;
        this.actorId = actorId;
        this.actorName = actorName;
        this.ipAddress = ipAddress;
        this.result = result;
        this.failReason = failReason;
    }

    public static LoginEvent success(String actorType, Long actorId, String actorName, String ipAddress) {
        return new LoginEvent(actorType, actorId, actorName, ipAddress, "SUCCESS", null);
    }

    public static LoginEvent fail(String actorType, String actorName, String ipAddress, String failReason) {
        return new LoginEvent(actorType, -1L, actorName, ipAddress, "FAIL", failReason);
    }
}
