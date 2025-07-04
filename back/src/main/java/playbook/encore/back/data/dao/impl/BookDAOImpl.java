package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.BookDAO;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.repository.BookRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class BookDAOImpl implements BookDAO {

    private final BookRepository bookRepository;

    @Autowired
    public BookDAOImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }


    @Override
    public Book insertBook(Book book) {
        Book savedBook = bookRepository.save(book);
        return savedBook;
    }

    @Override
    public Page<Book> selectBookListByPage(int page) {
        int pageSize = 10;
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("seqBook").ascending());
        return bookRepository.findAll(pageRequest);
    }


    @Override
    public Book selectBookById(Integer bookId) throws Exception {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isPresent()) {
            Book selectedBook = optionalBook.get();
            return selectedBook;
        } else {
            throw new IllegalArgumentException("해당 도서는 존재하지 않습니다.");
        }
    }

    @Override
    public Book updateBook(Book book) throws Exception {
        Book selectedBook = bookRepository.findById(book.getSeqBook())
        .orElseThrow(() -> new IllegalArgumentException("해당 도서는 존재하지 않습니다."));

        Book updatedBook;
        selectedBook.setSeqSortSecond(book.getSeqSortSecond());
        updatedBook = bookRepository.save(selectedBook);
        return updatedBook;
    }

    @Override
    public void deleteBook(Book book) throws Exception {
        Optional<Book> optionalBook = bookRepository.findById(book.getSeqBook());

        if (optionalBook.isPresent()) {
            Book selectedBook = optionalBook.get();

            bookRepository.delete(selectedBook);
        } else {
            throw new IllegalArgumentException("해당 도서를 삭제하지 못했습니다." + book.getSeqBook());
        }
    }

    @Override
    public List<Book> searchBooksRelated(String titleBook) throws Exception {
        List<Book> bookList = bookRepository.findByTitleBookContaining(titleBook);
        return bookList;
    }

    @Override
    public List<Book> searchBooksResult(String titleBook) throws Exception {
        return bookRepository.findByTitleBookContaining(titleBook);
    }
}
