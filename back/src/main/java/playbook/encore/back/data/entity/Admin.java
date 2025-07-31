package playbook.encore.back.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_admin", nullable = false)
    private Integer seqAdmin;

    @Column(name = "id_admin", nullable = false)
    private String idAdmin;

    @Column(name = "pw_admin", nullable = false)
    private String pwAdmin;

    @Column(name = "name_admin", nullable = false)
    private String nameAdmin;

    @Column(name = "dc_admin", nullable = false)
    private String dcAdmin;

    @Column(name = "agree_terms_admin", nullable = false)
    private String agreeTermsAdmin;

    @Column(name = "agree_info_admin", nullable = false)
    private String agreeInfoAdmin;

    @Column(name = "agree_discord_alarm_admin", nullable = false)
    private boolean agreeDiscordAlarmAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_admin", nullable = false)
    private StatusTypeAdmin statusAdmin;

    public enum StatusTypeAdmin {
        stop, available, overdue
    }
    
}
