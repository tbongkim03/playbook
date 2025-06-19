package playbook.encore.back.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "tb_book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_book", nullable = false)
    private Integer seqBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_sort_second", nullable = false)
    private SortSecond seqSortSecond;

    @Column(name = "isbn_book", nullable = false)
    private String isbnBook;

    @Column(name = "title_book", nullable = false)
    private String titleBook;

    @Column(name = "author_book", nullable = false)
    private String authorBook;

    @Column(name = "publisher_book", nullable = false)
    private String publisherBook;

    @Column(name = "publish_date_book", nullable = false)
    private LocalDate publishDateBook;

    @Column(name = "barcode_book", nullable = false)
    private String barcodeBook;

    @Column(name = "cnt_book", nullable = false)
    private Integer cntBook;

}
