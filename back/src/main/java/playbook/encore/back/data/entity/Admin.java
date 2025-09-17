package playbook.encore.back.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private boolean agreeTermsAdmin;

    @Column(name = "agree_info_admin", nullable = false)
    private boolean agreeInfoAdmin;

    @Column(name = "agree_discord_alarm_admin", nullable = false)
    private boolean agreeDiscordAlarmAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_admin", nullable = false)
    private StatusTypeAdmin statusAdmin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    public enum StatusTypeAdmin {
        stop, available, overdue
    }
    
}
