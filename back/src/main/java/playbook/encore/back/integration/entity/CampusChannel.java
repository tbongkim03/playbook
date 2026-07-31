package playbook.encore.back.integration.entity;

import jakarta.persistence.*;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * 캠퍼스별 디스코드 매핑
 * - discordChannelId : 알림이 발송될 캠퍼스 채널
 * - discordRoleId    : 플북 연동 시 부여되어 캠퍼스 채널을 해금하는 역할
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "use_yn = 'Y'")
@Table(name = "tb_campus_channel")
public class CampusChannel extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_campus_channel", nullable = false)
    private Integer seqCampusChannel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_campus", nullable = false, unique = true)
    private Campus seqCampus;

    @Column(name = "discord_channel_id", length = 40)
    private String discordChannelId;

    @Column(name = "discord_role_id", length = 40)
    private String discordRoleId;
}
