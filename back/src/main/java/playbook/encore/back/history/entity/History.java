package playbook.encore.back.history.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.course.entity.Course;
import playbook.encore.back.book.entity.Book;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_history")
public class History extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_history", nullable = false)
    private Integer seqHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_campus", nullable = false)
    private Campus seqCampus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_admin")
    private Admin seqAdmin;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_user")
    private BookUser seqUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_course")
    private Course seqCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_book")
    private Book seqBook;
    
    @Column(name = "book_dt", nullable = false)
    private LocalDate bookDt;

    @Column(name = "return_dt")
    private LocalDate returnDt;

    public enum StatusType {
        booked , returned, overdue
    }
}
