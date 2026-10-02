package playbook.encore.back.auditlog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_audit_log")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_audit_log")
    private Long seqAuditLog;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "actor_name", nullable = false, length = 30)
    private String actorName;

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_id", length = 50)
    private String targetId;

    @Column(name = "detail", length = 255)
    private String detail;

    @Column(name = "result", nullable = false, length = 10)
    private String result;

    @Column(name = "fail_reason", length = 100)
    private String failReason;

    @Column(name = "audited_at", nullable = false)
    private LocalDateTime auditedAt;

    @PrePersist
    protected void prePersist() {
        if (auditedAt == null) {
            auditedAt = LocalDateTime.now();
        }
    }
}
