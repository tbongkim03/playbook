package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.Book;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

public interface BookDAO {
    Book insertBook(Book book);

    Page<Book> selectBookListByPage(int page) throws Exception;

    Book selectBookById(Integer bookId) throws Exception;

    Book updateBook(Book book) throws Exception;

    void deleteBook(Book book) throws Exception;

    List<Book> searchBooksRelated(String titleBook) throws Exception;

    List<Book> searchBooksResultExact(String titleBook) throws Exception;
    
    List<Book> searchBooksResultContaining(String titleBook) throws Exception;
}
