package playbook.encore.back.admin.entity;

import jakarta.persistence.*;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "use_yn = 'Y'")
@Table(name = "tb_admin")
public class Admin extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_admin", nullable = false)
    private Integer seqAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_campus")
    private Campus seqCampus;

    @Column(name = "id_admin", nullable = false)
    private String idAdmin;

    @Column(name = "pw_admin", nullable = false)
    private String pwAdmin;

    @Column(name = "name_admin", nullable = false)
    private String nameAdmin;

    @Column(name = "dc_admin", nullable = false)
    private String dcAdmin;

    @Column(name = "agree_terms_admin", nullable = false)
    private boolean agreeTermsAdmin;

    @Column(name = "agree_info_admin", nullable = false)
    private boolean agreeInfoAdmin;

    @Column(name = "agree_discord_alarm_admin", nullable = false)
    private boolean agreeDiscordAlarmAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_admin", nullable = false)
    private StatusTypeAdmin statusAdmin;

    public enum StatusTypeAdmin {
        stop, available, overdue
    }
    
}
