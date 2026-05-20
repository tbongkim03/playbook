package playbook.encore.back.campus.entity;

import jakarta.persistence.*;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_campus")
public class Campus extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_campus", nullable = false)
    private Integer seqCampus;

    @Column(name = "name_campus", nullable = false, unique = true)
    private String nameCampus;

    @Column(name = "location_campus")
    private String locationCampus;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
