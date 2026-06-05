package playbook.encore.back.common.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
@Setter
public class AuditContext {
    private Long actorId;
    private String actorName;
    private String actorType; // "USER" or "ADMIN"
}
