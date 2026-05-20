package playbook.encore.back.sort.entity;

import jakarta.persistence.*;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "tb_sort_second")
public class SortSecond extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_sort_second", nullable = false)
    private Integer seqSortSecond;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_sort_first", nullable = false)
    private SortFirst seqSortFirst;

    @Column(name = "kor_sort_second", nullable = false)
    private String korSortSecond;

    @Column(name = "name_sort_second", nullable = false)
    private String nameSortSecond;
}
