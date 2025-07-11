package playbook.encore.back.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "tb_sort_first")
public class SortFirst {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_sort_first", nullable = false)
    private Integer seqSortFirst;

    @Column(name = "korSortFirst", nullable = false)
    private String korSortFirst;

    @Column(name = "name_sort_first", nullable = false)
    private String nameSortFirst;
}
