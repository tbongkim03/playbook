package playbook.encore.back.book.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import playbook.encore.back.book.dao.BookDAO;
import playbook.encore.back.book.entity.Book;
import playbook.encore.back.book.dao.BookRepository;

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
        try {
            Book savedBook = bookRepository.save(book);
            return savedBook;
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();

            if (errorMessage != null && errorMessage.contains("Data too long for column")) {
                // 어느 컬럼에서 문제가 발생했는지 파악
                if (errorMessage.contains("author_book")) {
                    throw new IllegalArgumentException("저자명이 너무 깁니다. 최대 20자까지 입력 가능합니다.");
                } else if (errorMessage.contains("title_book")) {
                    throw new IllegalArgumentException("책 제목이 너무 깁니다. 최대 255자까지 입력 가능합니다.");
                } else if (errorMessage.contains("publisher_book")) {
                    throw new IllegalArgumentException("출판사명이 너무 깁니다. 최대 20자까지 입력 가능합니다.");
                }
                throw new IllegalArgumentException("입력된 데이터가 허용된 길이를 초과했습니다.");
            }

            throw new IllegalArgumentException("데이터 저장 중 오류가 발생했습니다: " + (errorMessage != null ? errorMessage : e.getMessage()));
        }
    }

    @Override
    public List<Book> selectBookListAll() {
        return bookRepository.findAllWithNonZeroSeqSortSecondAndPrintCheckBookTrue();
    }

    @Override
    public List<Book> selectAllBooks() {
        return bookRepository.findAllWithCategories();
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
    public List<Book> selectBookListBySortFirst(int sortFirstId) {
        String jpql = "SELECT b FROM Book b " +
                "JOIN b.seqSortSecond ss " +
                "JOIN ss.seqSortFirst sf " +
                "WHERE sf.seqSortFirst = :sortFirstId AND ss.seqSortSecond != 0 " +
                "ORDER BY b.seqBook ASC";

        return entityManager.createQuery(jpql, Book.class)
                .setParameter("sortFirstId", sortFirstId)
                .getResultList();
    }

    @Override
    public List<Book> selectBookListBySortFirstAndCampus(int sortFirstId, Integer campusId) {
        String jpql = "SELECT b FROM Book b " +
                "JOIN b.seqSortSecond ss " +
                "JOIN ss.seqSortFirst sf " +
                "WHERE sf.seqSortFirst = :sortFirstId AND ss.seqSortSecond != 0 " +
                "AND b.seqCampus.seqCampus = :campusId " +
                "ORDER BY b.seqBook ASC";

        return entityManager.createQuery(jpql, Book.class)
                .setParameter("sortFirstId", sortFirstId)
                .setParameter("campusId", campusId)
                .getResultList();
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

    // 캠퍼스별 연관검색어
    @Override
    public List<Book> searchBooksRelatedByCampus(String titleBook, Integer campusId) throws Exception {
        return bookRepository.findByTitleBookContainingAndSeqCampus_SeqCampusAndSeqSortSecond_SeqSortSecondNot(titleBook, campusId, 0);
    }

    // 캠퍼스별 정확한 제목 검색
    @Override
    public List<Book> searchBooksResultExactByCampus(String titleBook, Integer campusId) throws Exception {
        return bookRepository.findByTitleBookAndSeqCampus_SeqCampusAndSeqSortSecond_SeqSortSecondNot(titleBook, campusId, 0);
    }

    // 캠퍼스별 제목 포함 검색
    @Override
    public List<Book> searchBooksResultContainingByCampus(String titleBook, Integer campusId) throws Exception {
        return bookRepository.findByTitleBookContainingAndSeqCampus_SeqCampusAndSeqSortSecond_SeqSortSecondNot(titleBook, campusId, 0);
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

    @Override
    public Page<Book> selectBookListAllWithPagination(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);

        String jpql = "SELECT b FROM Book b " +
                "JOIN FETCH b.seqCampus " +
                "JOIN FETCH b.seqSortSecond " +
                "WHERE b.seqSortSecond.seqSortSecond != 0 AND b.printCheckBook = true";

        String countJpql = "SELECT COUNT(b) FROM Book b " +
                "WHERE b.seqSortSecond.seqSortSecond != 0 AND b.printCheckBook = true";

        // 정렬 적용을 위한 동적 쿼리
        if (sortBy.equals("seqBook")) {
            jpql += " ORDER BY b.seqBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else if (sortBy.equals("titleBook")) {
            jpql += " ORDER BY b.titleBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else if (sortBy.equals("authorBook")) {
            jpql += " ORDER BY b.authorBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else if (sortBy.equals("borrowCount")) {
            // borrowCount는 History에서 계산해야 하므로 기본 정렬 사용
            jpql += " ORDER BY b.seqBook DESC";
        } else {
            jpql += " ORDER BY b.seqBook DESC"; // 기본값
        }

        List<Book> books = entityManager.createQuery(jpql, Book.class)
                .setFirstResult((int) pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();

        Long total = entityManager.createQuery(countJpql, Long.class)
                .getSingleResult();

        return new PageImpl<>(books, pageRequest, total);
    }

    @Override
    public Page<Book> selectBookListAllWithPaginationByCampus(Integer campusId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);

        String jpql = "SELECT b FROM Book b " +
                "JOIN FETCH b.seqCampus " +
                "JOIN FETCH b.seqSortSecond " +
                "WHERE b.seqCampus.seqCampus = :campusId " +
                "AND b.seqSortSecond.seqSortSecond != 0 AND b.printCheckBook = true";

        String countJpql = "SELECT COUNT(b) FROM Book b " +
                "WHERE b.seqCampus.seqCampus = :campusId " +
                "AND b.seqSortSecond.seqSortSecond != 0 AND b.printCheckBook = true";

        // 정렬 적용
        if (sortBy.equals("seqBook")) {
            jpql += " ORDER BY b.seqBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else if (sortBy.equals("titleBook")) {
            jpql += " ORDER BY b.titleBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else if (sortBy.equals("authorBook")) {
            jpql += " ORDER BY b.authorBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else {
            jpql += " ORDER BY b.seqBook DESC"; // 기본값
        }

        List<Book> books = entityManager.createQuery(jpql, Book.class)
                .setParameter("campusId", campusId)
                .setFirstResult((int) pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();

        Long total = entityManager.createQuery(countJpql, Long.class)
                .setParameter("campusId", campusId)
                .getSingleResult();

        return new PageImpl<>(books, pageRequest, total);
    }

    @Override
    public Page<Book> selectBookListBySortFirstWithPagination(int sortFirstId, Integer campusId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);

        String jpql = "SELECT b FROM Book b " +
                "JOIN FETCH b.seqCampus " +
                "JOIN FETCH b.seqSortSecond ss " +
                "JOIN ss.seqSortFirst sf " +
                "WHERE sf.seqSortFirst = :sortFirstId AND ss.seqSortSecond != 0 AND b.printCheckBook = true";

        String countJpql = "SELECT COUNT(b) FROM Book b " +
                "JOIN b.seqSortSecond ss " +
                "JOIN ss.seqSortFirst sf " +
                "WHERE sf.seqSortFirst = :sortFirstId AND ss.seqSortSecond != 0 AND b.printCheckBook = true";

        if (campusId != null) {
            jpql += " AND b.seqCampus.seqCampus = :campusId";
            countJpql += " AND b.seqCampus.seqCampus = :campusId";
        }

        // 정렬 적용
        if (sortBy.equals("seqBook")) {
            jpql += " ORDER BY b.seqBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else if (sortBy.equals("titleBook")) {
            jpql += " ORDER BY b.titleBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else if (sortBy.equals("authorBook")) {
            jpql += " ORDER BY b.authorBook " + (sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        } else {
            jpql += " ORDER BY b.seqBook DESC"; // 기본값
        }

        var query = entityManager.createQuery(jpql, Book.class)
                .setParameter("sortFirstId", sortFirstId)
                .setFirstResult((int) pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize());

        var countQuery = entityManager.createQuery(countJpql, Long.class)
                .setParameter("sortFirstId", sortFirstId);

        if (campusId != null) {
            query.setParameter("campusId", campusId);
            countQuery.setParameter("campusId", campusId);
        }

        List<Book> books = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(books, pageRequest, total);
    }
}
