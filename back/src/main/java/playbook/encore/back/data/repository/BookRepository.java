package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import playbook.encore.back.data.entity.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Integer countByIsbnBook(String isbnBook);
    List<Book> findByTitleBook(String titleBook);
    List<Book> findByTitleBookContaining(String titleBook);
    
    @Modifying
    @Query("UPDATE Book b SET b.printCheckBook = true WHERE b.seqBook IN :ids")
    int markAsPrintedByIds(@Param("ids") List<Integer> ids);

    List<Book> findByPrintCheckBookFalseAndSeqSortSecond_SeqSortSecondNotAndCntBookIsNotNullAndBarcodeBookIsNotNull(Integer seqSortSecond);

}
