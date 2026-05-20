package playbook.encore.back.favor.entity;

import jakarta.persistence.*;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.book.entity.Book;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "use_yn = 'Y'")
@Table(name = "tb_favor")
public class Favor extends BaseAuditEntity {
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
