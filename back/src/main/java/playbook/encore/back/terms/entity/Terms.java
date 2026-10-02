package playbook.encore.back.terms.entity;

import jakarta.persistence.*;
import lombok.*;
import playbook.encore.back.common.audit.BaseAuditEntity;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_terms")
public class Terms extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_terms")
    private Integer seqTerms;

    @Column(name = "terms_type", nullable = false, unique = true, length = 30)
    private String termsType;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;
}
