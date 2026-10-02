package playbook.encore.back.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
public abstract class BaseAuditEntity {

    @Column(name = "use_yn", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String useYn = "Y";

    @Column(name = "created_by_type", length = 20)
    private String createdByType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by_type", length = 20)
    private String updatedByType;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}
