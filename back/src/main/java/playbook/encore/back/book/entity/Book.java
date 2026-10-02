package playbook.encore.back.book.entity;

import jakarta.persistence.*;
import playbook.encore.back.sort.entity.SortSecond;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.common.audit.BaseAuditEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "use_yn = 'Y'")
@Table(name = "tb_book")
public class Book extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_book", nullable = false)
    private Integer seqBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_campus", nullable = false)
    private Campus seqCampus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_sort_second", nullable = false)
    private SortSecond seqSortSecond;

    @Column(name = "isbn_book", nullable = false, length = 20)
    private String isbnBook;

    @Column(name = "title_book", nullable = false, length = 255)
    private String titleBook;

    @Column(name = "author_book", nullable = false, length = 20)
    private String authorBook;

    @Column(name = "publisher_book", nullable = false, length = 20)
    private String publisherBook;

    @Column(name = "publish_date_book", nullable = false)
    private LocalDate publishDateBook;

    @Column(name = "img_url_book", nullable = false, length = 255)
    private String imgUrlBook;

    @Column(name = "barcode_book", length = 30)
    private String barcodeBook;

    @Column(name = "cnt_book")
    private Integer cntBook;

    @Column(name = "print_check_book", nullable = false)
    private boolean printCheckBook;

    @Column(name = "is_book_borrowed", nullable = false, columnDefinition = "boolean default false")
    private boolean isBookBorrowed; // 대여 중인 책인지 여부, true면 대여 중, false면 대여 가능
}

