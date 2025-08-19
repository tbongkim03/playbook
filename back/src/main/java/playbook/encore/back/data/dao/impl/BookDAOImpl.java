package playbook.encore.back.data.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.BookDAO;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.repository.BookRepository;

import java.util.List;
import java.util.Optional;

@Component
public class BookDAOImpl implements BookDAO {

    private final BookRepository bookRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
        return bookRepository.findAllWithNonZeroSeqSortSecond(pageRequest);
    }

    @Override
    public Page<Book> selectAllBooks(int page) {
        int pageSize = 10;
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("seqBook").ascending());
        return bookRepository.findAll(pageRequest);
    }

    @Override
    public Page<Book> selectBookListByPageBySortFirst(int sortFirstId, int page) {
        int pageSize = 20;
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("seqBook").ascending());

        String jpql = "SELECT b FROM Book b " +
                "JOIN b.seqSortSecond ss " +
                "JOIN ss.seqSortFirst sf " +
                "WHERE sf.seqSortFirst = :sortFirstId AND ss.seqSortSecond != 0";

        String countJpql = "SELECT COUNT(b) FROM Book b " +
                "JOIN b.seqSortSecond ss " +
                "JOIN ss.seqSortFirst sf " +
                "WHERE sf.seqSortFirst = :sortFirstId AND ss.seqSortSecond != 0";

        List<Book> books = entityManager.createQuery(jpql, Book.class)
                .setParameter("sortFirstId", sortFirstId)
                .setFirstResult((int) pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();

        Long total = entityManager.createQuery(countJpql, Long.class)
                .setParameter("sortFirstId", sortFirstId)
                .getSingleResult();

        return new PageImpl<>(books, pageRequest, total);
    }

    @Override
    public Book selectBookById(int bookId, String idUser) throws Exception {
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
        selectedBook.setBarcodeBook(book.getBarcodeBook());
        selectedBook.setCntBook(book.getCntBook());
        selectedBook.setPrintCheckBook(book.isPrintCheckBook());
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
            throw new IllegalArgumentException("해당 도서를 삭제하지 못했습니다. : " + book.getTitleBook());
        }
    }

    // 연관검색어
    @Override
    public List<Book> searchBooksRelated(String titleBook) throws Exception {
        List<Book> bookList = bookRepository.findByTitleBookContainingAndSeqSortSecond_SeqSortSecondNot(titleBook, 0);
        return bookList;
    }

    // 정확한 제목 검색
    @Override
    public List<Book> searchBooksResultExact(String titleBook) throws Exception {
        return bookRepository.findByTitleBookAndSeqSortSecond_SeqSortSecondNot(titleBook, 0);
    }

    // 제목 포함 검색
    @Override
    public List<Book> searchBooksResultContaining(String titleBook) throws Exception {
        return bookRepository.findByTitleBookContainingAndSeqSortSecond_SeqSortSecondNot(titleBook, 0);
    }

    @Override
    public void printPost(List<Integer> bookIds) throws Exception {
        bookRepository.markAsPrintedByIds(bookIds);
    }

    @Override
    public List<Book> findUnprintedBooks() throws Exception {
        return bookRepository.findByPrintCheckBookFalseAndSeqSortSecond_SeqSortSecondNotAndCntBookIsNotNullAndBarcodeBookIsNotNull(0);
    }

    @Override
    public boolean checkDuplicates(int seqBook, String barcodeBook) throws Exception {
        return bookRepository.existsByBarcodeBookAndSeqBookNot(barcodeBook, seqBook);
    }

    @Override
    public Book bookStatusUpdate(Book book, boolean status) throws Exception {
        Book selectedBook = bookRepository.findById(book.getSeqBook())
                .orElseThrow(() -> new IllegalArgumentException("해당 도서는 존재하지 않습니다."));
        selectedBook.setBookBorrowed(status);
        return bookRepository.save(selectedBook);
    }
}
