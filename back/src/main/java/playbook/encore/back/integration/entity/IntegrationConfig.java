package playbook.encore.back.integration.entity;

import jakarta.persistence.*;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * 외부 연동 글로벌 설정 (key-value)
 * 디스코드 봇 토큰·링크 채널, Work24·Naver API 키 등 단일 값.
 * is_secret = true 인 값은 config_value 에 암호화되어 저장된다.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "use_yn = 'Y'")
@Table(name = "tb_integration_config")
public class IntegrationConfig extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_config", nullable = false)
    private Integer seqConfig;

    @Column(name = "config_key", nullable = false, unique = true, length = 60)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "is_secret", nullable = false)
    @Builder.Default
    private boolean isSecret = false;

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "description", length = 200)
    private String description;
}
