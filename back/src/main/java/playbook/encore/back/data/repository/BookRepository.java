package playbook.encore.back.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import playbook.encore.back.data.entity.Book;


import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Integer countByIsbnBook(String isbnBook);
    List<Book> findByTitleBookContainingAndSeqSortSecond_SeqSortSecondNot(String titleBook, Integer seqSortSecond);
    List<Book> findByTitleBookAndSeqSortSecond_SeqSortSecondNot(String titleBook, Integer seqSortSecond);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.seqSortSecond ss LEFT JOIN FETCH ss.seqSortFirst")
    List<Book> findAllWithCategories();

    @Query("SELECT b FROM Book b WHERE b.seqSortSecond.seqSortSecond != 0")
    Page<Book> findAllWithNonZeroSeqSortSecond(Pageable pageable);
    
    @Modifying
    @Query("UPDATE Book b SET b.printCheckBook = true WHERE b.seqBook IN :ids")
    int markAsPrintedByIds(@Param("ids") List<Integer> ids);

    List<Book> findByPrintCheckBookFalseAndSeqSortSecond_SeqSortSecondNotAndCntBookIsNotNullAndBarcodeBookIsNotNull(Integer seqSortSecond);

    boolean existsByBarcodeBookAndSeqBookNot(String barcodeBook, Integer seqBook);

    Optional<Book> findByBarcodeBook(String barcodeBook);
}
