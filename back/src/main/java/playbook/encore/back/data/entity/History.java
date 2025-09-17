package playbook.encore.back.data.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_history")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_history", nullable = false)
    private Integer seqHistory;

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
