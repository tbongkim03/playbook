package playbook.encore.back.sort.entity;

import jakarta.persistence.*;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Where(clause = "use_yn = 'Y'")
@Table(name = "tb_sort_first")
public class SortFirst extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_sort_first", nullable = false)
    private Integer seqSortFirst;

    @Column(name = "korSortFirst", nullable = false)
    private String korSortFirst;

    @Column(name = "name_sort_first", nullable = false)
    private String nameSortFirst;
}
