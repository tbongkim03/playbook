package playbook.encore.back.book.dao;

import playbook.encore.back.book.entity.Book;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

public interface BookDAO {
    Book insertBook(Book book);

    List<Book> selectBookListAll() throws Exception;

    List<Book> selectAllBooks() throws Exception;

    Book selectBookById(int bookId, String idUser) throws Exception;

    Book updateBook(Book book) throws Exception;

    void deleteBook(Book book) throws Exception;

    List<Book> searchBooksRelated(String titleBook) throws Exception;

    List<Book> searchBooksResultExact(String titleBook) throws Exception;

    List<Book> searchBooksResultContaining(String titleBook) throws Exception;

    // 캠퍼스별 검색 메서드 추가
    List<Book> searchBooksRelatedByCampus(String titleBook, Integer campusId) throws Exception;

    List<Book> searchBooksResultExactByCampus(String titleBook, Integer campusId) throws Exception;

    List<Book> searchBooksResultContainingByCampus(String titleBook, Integer campusId) throws Exception;

    void printPost(List<Integer> bookIds) throws Exception;

    List<Book> findUnprintedBooks() throws Exception;

    boolean checkDuplicates(int seqBook, String barcodeBook) throws Exception;

    Page<Book> selectBookListByPageBySortFirst(int sortFirstId, int page);

    List<Book> selectBookListBySortFirst(int sortFirstId);

    List<Book> selectBookListBySortFirstAndCampus(int sortFirstId, Integer campusId);

    // 페이지네이션 메서드 추가
    Page<Book> selectBookListAllWithPagination(int page, int size, String sortBy, String sortDir);
    
    Page<Book> selectBookListAllWithPaginationByCampus(Integer campusId, int page, int size, String sortBy, String sortDir);
    
    Page<Book> selectBookListBySortFirstWithPagination(int sortFirstId, Integer campusId, int page, int size, String sortBy, String sortDir);

    Page<Book> selectBookListBySortSecondWithPagination(int sortSecondId, Integer campusId, int page, int size, String sortBy, String sortDir);

    Book bookStatusUpdate(Book book, boolean status) throws Exception;

    Page<Book> selectAdminBookListWithFilters(
            Integer campusId, String search, Integer seqSortFirst, Integer seqSortSecond,
            String borrowStatus, Integer registerYear, Integer registerMonth,
            LocalDate registerStartDate, LocalDate registerEndDate,
            int page, int size, String sortBy, String sortDir);
}
