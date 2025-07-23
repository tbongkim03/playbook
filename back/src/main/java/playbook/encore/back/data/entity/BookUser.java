package playbook.encore.back.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_user")
public class BookUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_user", nullable = false)
    private Integer seqUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_course", nullable = false)
    private Course seqCourse;

    @Column(name = "id_user", nullable = false)
    private String idUser;

    @Column(name = "pw_user", nullable = false)
    private String pwUser;

    @Column(name = "name_user", nullable = false)
    private String nameUser;

    @Column(name = "dc_user", nullable = false)
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
