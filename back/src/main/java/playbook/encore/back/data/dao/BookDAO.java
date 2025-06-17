package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.Book;

import java.util.List;

public interface BookDAO {
    Book insertBook(Book book);

    List<Book> selectAllBook() throws Exception;

    Book selectBookById(Integer bookId) throws Exception;

    Book updateBook(Book book) throws Exception;

    void deleteBook(Book book) throws Exception;
}
