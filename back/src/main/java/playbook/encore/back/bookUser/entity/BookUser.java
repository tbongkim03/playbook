package playbook.encore.back.bookUser.entity;

import jakarta.persistence.*;
import playbook.encore.back.course.entity.Course;
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
@Table(name = "tb_user")
public class BookUser extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_user", nullable = false)
    private Integer seqUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_course", nullable = true)
    private Course seqCourse;

    @Column(name = "id_user", nullable = false, length = 30)
    private String idUser;

    @Column(name = "pw_user", nullable = false, length = 255)
    private String pwUser;

    @Column(name = "name_user", nullable = false, length = 20)
    private String nameUser;

    @Column(name = "dc_user", nullable = false, length = 30)
    private String dcUser;

    @Column(name = "agree_terms_user", nullable = false)
    private boolean agreeTermsUser;

    @Column(name = "agree_info_user", nullable = false)
    private boolean agreeInfoUser;

    @Column(name = "agree_discord_alarm_user", nullable = false)
    private boolean agreeDiscordAlarmUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_user", nullable = false)
    private StatusType statusUser;

    public enum StatusType {
        stop, available, overdue 
    }
}
