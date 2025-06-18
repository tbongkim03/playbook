package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.Book;

import java.time.LocalDate;
import java.util.List;

public interface BookDAO {
    Book insertBook(Book book);

    List<Book> selectAllBook() throws Exception;

    Book selectBookById(Integer bookId) throws Exception;

    Book updateBook(Integer bookId, Integer seqSortSecond, String isbnBook, String titleBook,
                    String authorBook, String publisherBook, LocalDate publishDateBook, String barcodeBook,
                    Integer cntBook) throws Exception;

    void deleteBook(Book book) throws Exception;
}
