package playbook.encore.back.common.audit;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;

public class AuditEntityListener {

    @PrePersist
    public void prePersist(BaseAuditEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        AuditContext ctx = getAuditContext();
        if (ctx != null) {
            entity.setCreatedBy(ctx.getActorId());
            entity.setCreatedByType(ctx.getActorType());
            entity.setUpdatedBy(ctx.getActorId());
            entity.setUpdatedByType(ctx.getActorType());
        }
    }

    @PreUpdate
    public void preUpdate(BaseAuditEntity entity) {
        entity.setUpdatedAt(LocalDateTime.now());
        AuditContext ctx = getAuditContext();
        if (ctx != null) {
            entity.setUpdatedBy(ctx.getActorId());
            entity.setUpdatedByType(ctx.getActorType());
        }
    }

    private AuditContext getAuditContext() {
        try {
            ApplicationContext context = ApplicationContextProvider.getContext();
            if (context == null) return null;
            return context.getBean(AuditContext.class);
        } catch (Exception e) {
            return null;
        }
    }
}
