package playbook.encore.back.accesslog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_access_log")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_access_log")
    private Long seqAccessLog;

    @Column(name = "actor_type", nullable = false, length = 10)
    private String actorType;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "actor_name", nullable = false, length = 30)
    private String actorName;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "result", nullable = false, length = 10)
    private String result;

    @Column(name = "fail_reason", length = 100)
    private String failReason;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;

    @PrePersist
    protected void prePersist() {
        if (accessedAt == null) {
            accessedAt = LocalDateTime.now();
        }
    }
}
