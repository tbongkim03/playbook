package playbook.encore.back.allowip.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.common.audit.BaseAuditEntity;

/**
 * 접속 허용 IP 규칙.
 * 단일 IP(SINGLE) 또는 CIDR 대역(CIDR)을 저장하며, seq_campus 는 관리 편의용 라벨이다.
 * (차단 판정은 IpAllowlistFilter 가 활성 규칙 전체를 OR 로 본다)
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "use_yn = 'Y'")
// 005_add_allowed_ip.sql 의 제약과 같은 이름 — ddl-auto 로 만든 스키마(CI 등)에도 동일하게 걸리게 한다
@Table(name = "tb_allowed_ip",
        uniqueConstraints = @UniqueConstraint(name = "uk_allowed_ip_value_campus",
                columnNames = {"ip_value", "seq_campus"}))
public class AllowedIp extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_allowed_ip", nullable = false)
    private Integer seqAllowedIp;

    /** 단일 IP 또는 CIDR 표기 (예: 192.168.0.15, 192.168.0.0/24) */
    @Column(name = "ip_value", nullable = false, length = 64)
    private String ipValue;

    /** SINGLE | CIDR — 서버가 ipValue 로부터 판정해 저장 */
    @Column(name = "ip_type", nullable = false, length = 10)
    private String ipType;

    /** null = 전역 규칙(모든 캠퍼스 적용) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_campus")
    private Campus seqCampus;

    @Column(name = "description", length = 200)
    private String description;

    /** false 면 규칙은 유지하되 차단 판정에서 제외 */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /** 설치 마법사·부트스트랩이 심은 규칙. 삭제 불가, 비활성만 가능 */
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean isSystem = false;
}
