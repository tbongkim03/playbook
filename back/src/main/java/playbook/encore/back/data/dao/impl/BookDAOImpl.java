package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Book> selectAllBook() throws Exception {
        List<Book> bookList = bookRepository.findAll();
        return bookList;
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
    public Book updateBook(Integer bookId, Integer seqSortSecond, String isbnBook, String titleBook,
                           String authorBook, String publisherBook, LocalDate publishDateBook, String barcodeBook,
                           Integer cntBook) throws Exception {
        Optional<Book> optionalBook = bookRepository.findById(bookId);

        Book updatedBook;
        if (optionalBook.isPresent()) {
            Book selectedBook = optionalBook.get();

            selectedBook.setIsbnBook(isbnBook);
            selectedBook.setTitleBook(titleBook);
            selectedBook.setAuthorBook(authorBook);
            selectedBook.setPublisherBook(publisherBook);
            selectedBook.setPublishDateBook(publishDateBook);

            // 바코드 문자열 생성 로직 구현, 책 갯수 파악 로직 구현
            selectedBook.setBarcodeBook(barcodeBook);
            selectedBook.setCntBook(cntBook);

            updatedBook = bookRepository.save(selectedBook);
        } else {
            throw new IllegalArgumentException("업데이트에 실패하였습니다. 해당 도서가 존재하지 않습니다.");
        }
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
}
