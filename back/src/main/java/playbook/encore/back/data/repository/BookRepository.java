package playbook.encore.back.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import playbook.encore.back.data.entity.Book;


import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    // ========== 기존 메서드 (캠퍼스 무관) ==========
    Integer countByIsbnBook(String isbnBook);
    List<Book> findByTitleBookContainingAndSeqSortSecond_SeqSortSecondNot(String titleBook, Integer seqSortSecond);
    List<Book> findByTitleBookAndSeqSortSecond_SeqSortSecondNot(String titleBook, Integer seqSortSecond);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.seqSortSecond ss LEFT JOIN FETCH ss.seqSortFirst")
    List<Book> findAllWithCategories();

    @Query("SELECT b FROM Book b WHERE b.seqSortSecond.seqSortSecond != 0 AND b.printCheckBook = true")
    List<Book> findAllWithNonZeroSeqSortSecondAndPrintCheckBookTrue();

    @Modifying
    @Query("UPDATE Book b SET b.printCheckBook = true WHERE b.seqBook IN :ids")
    int markAsPrintedByIds(@Param("ids") List<Integer> ids);

    List<Book> findByPrintCheckBookFalseAndSeqSortSecond_SeqSortSecondNotAndCntBookIsNotNullAndBarcodeBookIsNotNull(Integer seqSortSecond);

    boolean existsByBarcodeBookAndSeqBookNot(String barcodeBook, Integer seqBook);

    Optional<Book> findByBarcodeBook(String barcodeBook);

    // ========== 캠퍼스 필터링 메서드 (추가) ==========

    /**
     * 캠퍼스별 도서 목록 조회 (카테고리 페치 조인)
     */
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.seqSortSecond ss " +
           "LEFT JOIN FETCH ss.seqSortFirst " +
           "WHERE b.seqCampus.seqCampus = :campusId")
    List<Book> findAllWithCategoriesByCampus(@Param("campusId") Integer campusId);

    /**
     * 캠퍼스별 프린트 완료 도서 조회
     */
    @Query("SELECT b FROM Book b WHERE b.seqCampus.seqCampus = :campusId " +
           "AND b.seqSortSecond.seqSortSecond != 0 AND b.printCheckBook = true")
    List<Book> findAllWithNonZeroSeqSortSecondAndPrintCheckBookTrueByCampus(@Param("campusId") Integer campusId);

    /**
     * 캠퍼스별 바코드로 도서 조회
     */
    Optional<Book> findByBarcodeBookAndSeqCampus_SeqCampus(String barcodeBook, Integer campusId);

    /**
     * 캠퍼스별 바코드 중복 확인
     */
    boolean existsByBarcodeBookAndSeqCampus_SeqCampusAndSeqBookNot(
        String barcodeBook, Integer campusId, Integer seqBook);

    /**
     * 캠퍼스별 ISBN 카운트
     */
    Integer countByIsbnBookAndSeqCampus_SeqCampus(String isbnBook, Integer campusId);

    /**
     * 캠퍼스별 제목 검색
     */
    List<Book> findByTitleBookContainingAndSeqCampus_SeqCampusAndSeqSortSecond_SeqSortSecondNot(
        String titleBook, Integer campusId, Integer seqSortSecond);

    /**
     * 캠퍼스별 정확한 제목 검색 (seqSortSecond != 0)
     */
    List<Book> findByTitleBookAndSeqCampus_SeqCampusAndSeqSortSecond_SeqSortSecondNot(
        String titleBook, Integer campusId, Integer seqSortSecond);
}
