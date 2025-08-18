package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.Book;

import java.util.List;

import org.springframework.data.domain.Page;

public interface BookDAO {
    Book insertBook(Book book);

    Page<Book> selectBookListByPage(int page) throws Exception;

    Page<Book> selectAllBooks(int page) throws Exception;

    Book selectBookById(int bookId) throws Exception;

    Book updateBook(Book book) throws Exception;

    void deleteBook(Book book) throws Exception;

    List<Book> searchBooksRelated(String titleBook) throws Exception;

    List<Book> searchBooksResultExact(String titleBook) throws Exception;
    
    List<Book> searchBooksResultContaining(String titleBook) throws Exception;

    void printPost(List<Integer> bookIds) throws Exception;

    List<Book> findUnprintedBooks() throws Exception;

    boolean checkDuplicates(int seqBook, String barcodeBook) throws Exception;

    Page<Book> selectBookListByPageBySortFirst(int sortFirstId, int page);

    Book bookStatusUpdate(Book book, boolean status) throws Exception;
}
