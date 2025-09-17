package playbook.encore.back.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_favor")
public class Favor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_favor", nullable = false)
    private Integer seqFavor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_user", nullable = false)
    private BookUser seqUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_book", nullable = false)
    private Book seqBook;
}
