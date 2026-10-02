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
        try {
            AuditContext ctx = getAuditContext();
            if (ctx != null) {
                entity.setCreatedBy(ctx.getActorId());
                entity.setCreatedByType(ctx.getActorType());
                entity.setUpdatedBy(ctx.getActorId());
                entity.setUpdatedByType(ctx.getActorType());
            }
        } catch (Exception e) {
            // request scope 없는 환경(앱 초기화 등)에서는 감사 정보 생략
        }
    }

    @PreUpdate
    public void preUpdate(BaseAuditEntity entity) {
        entity.setUpdatedAt(LocalDateTime.now());
        try {
            AuditContext ctx = getAuditContext();
            if (ctx != null) {
                entity.setUpdatedBy(ctx.getActorId());
                entity.setUpdatedByType(ctx.getActorType());
            }
        } catch (Exception e) {
            // request scope 없는 환경(앱 초기화 등)에서는 감사 정보 생략
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
