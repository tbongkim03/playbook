package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.BookDAO;
import playbook.encore.back.data.dao.HistoryDAO;
import playbook.encore.back.data.dto.book.BookBarcodeUniqueRequestDto;
import playbook.encore.back.data.dto.book.BookBarcodeUniqueResponseDto;
import playbook.encore.back.data.dto.book.BookCountResponseDto;
import playbook.encore.back.data.dto.book.BookListResponseDto;
import playbook.encore.back.data.dto.book.BookRequestDto;
import playbook.encore.back.data.dto.book.BookResponseDto;
import playbook.encore.back.data.dto.book.BookSearchResponseDto;
import playbook.encore.back.data.dto.book.BookSortAndBarcodeRequestDto;
import playbook.encore.back.data.dto.book.BookUnprintedResponseDto;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.SortSecond;
import playbook.encore.back.data.repository.BookRepository;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.data.repository.SortSecondRepository;
import playbook.encore.back.service.BookService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookDAO bookDAO;
    private final BookRepository bookRepository;
    private final SortSecondRepository sortSecondRepository;
    private final HistoryDAO historyDAO;
    private final BookUserRepository bookUserRepository;

    @Autowired
    public BookServiceImpl(BookDAO bookDAO, BookRepository bookRepository, SortSecondRepository sortSecondRepository, HistoryDAO historyDAO, BookUserRepository bookUserRepository) {
        this.bookDAO = bookDAO;
        this.bookRepository = bookRepository;
        this.sortSecondRepository = sortSecondRepository;
        this.historyDAO = historyDAO;
        this.bookUserRepository = bookUserRepository;
    }

    private BookResponseDto convertToDto(Book entity) {
        return BookResponseDto.builder()
                .seqBook(entity.getSeqBook())
                .seqSortSecond(entity.getSeqSortSecond().getSeqSortSecond())
                .isbnBook(entity.getIsbnBook())
                .titleBook(entity.getTitleBook())
                .authorBook(entity.getAuthorBook())
                .publisherBook(entity.getPublisherBook())
                .publishDateBook(String.valueOf(entity.getPublishDateBook()))
                .imageBook(entity.getImgUrlBook())
                .barcodeBook(entity.getBarcodeBook())
                .cntBook(entity.getCntBook())
                .printCheckBook(entity.isPrintCheckBook())
                .bookBorrowed(entity.isBookBorrowed())
                .isBorrowedByMe(false)
                .build();
    }

    @Override
    @Transactional
    public BookResponseDto insertBook(BookRequestDto bookRequestDto) {
        SortSecond sortSecond = sortSecondRepository.findById(bookRequestDto.getSeqSortSecond())
                .orElseThrow(() -> new IllegalArgumentException("해당 분류가 존재하지 않습니다"));
        // SortFirst sortFirst = sortSecond.getSeqSortFirst();

        Book book = Book.builder()
                .seqSortSecond(sortSecond)
                .isbnBook(bookRequestDto.getIsbnBook())
                .titleBook(bookRequestDto.getTitleBook())
                .authorBook(bookRequestDto.getAuthorBook())
                .publisherBook(bookRequestDto.getPublisherBook())
                .publishDateBook(bookRequestDto.getPublishDateBook())
                .imgUrlBook(bookRequestDto.getImageBook())
                .barcodeBook(null)
                .cntBook(null)
                .printCheckBook(false)
                .build();

        // 바코드 문자열 생성 로직 구현, 책 갯수 파악 로직 구현 deprecated

        Book savedBook = bookDAO.insertBook(book);
        BookResponseDto bookResponseDto = convertToDto(savedBook);

        return bookResponseDto;
    }

    @Override
    public BookListResponseDto getBookList(int page) throws Exception {
        Page<Book> bookPage = bookDAO.selectBookListByPage(page);

        List<BookResponseDto> content = bookPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int totalCount = (int) bookPage.getTotalElements();
        BookListResponseDto bookListResponseDto = new BookListResponseDto(content, totalCount);
        return bookListResponseDto;
    }

    @Override
    public BookListResponseDto getAllBooks(int page) throws Exception {
        Page<Book> books = bookDAO.selectAllBooks(page);

        List<BookResponseDto> content = books.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int totalCount = content.size();
        return new BookListResponseDto(content, totalCount);
    }

    @Override
    public BookListResponseDto getBookListBySortFirst(int sortFirstId, int page) {
        Page<Book> bookPage = bookDAO.selectBookListByPageBySortFirst(sortFirstId, page);

        List<BookResponseDto> content = bookPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        int totalCount = (int) bookPage.getTotalElements();
        BookListResponseDto bookListResponseDto = new BookListResponseDto(content, totalCount);
        return bookListResponseDto;
    }

    @Override
    public BookResponseDto getBookById(int bookId, String idUser) throws Exception {
        Book selectedBook = bookDAO.selectBookById(bookId, idUser);
        BookResponseDto bookResponseDto = convertToDto(selectedBook);

        // 로그인한 사용자인 경우, 해당 책을 빌렸는지 확인
        if (idUser != null) {
            // idUser로 seqUser 조회
            Integer userSeq = bookUserRepository.findByIdUser(idUser)
                    .map(BookUser::getSeqUser)
                    .orElse(null);
            if (userSeq != null) {
                boolean isBorrowedByMe = checkIfBookBorrowedByUser(bookId, userSeq);
                bookResponseDto.setBorrowedByMe(isBorrowedByMe);
            } else {
                bookResponseDto.setBorrowedByMe(false);
            }
        } else {
            bookResponseDto.setBorrowedByMe(false);
        }
        return bookResponseDto;
    }

    private boolean checkIfBookBorrowedByUser(int bookId, int userSeq) {
        boolean result = historyDAO.existsByBookIdAndSeqUserAndReturnDateIsNull(bookId, userSeq);
        return result;
    }

    // 프린트 함, 분류, 책 권수, 바코드 값 넣기.
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookResponseDto changeBook(int bookId, BookSortAndBarcodeRequestDto bookSortAndBarcodeRequestDto) throws Exception {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("해당 책이 존재하지 않습니다."));

        SortSecond sortSecond = sortSecondRepository.findById(bookSortAndBarcodeRequestDto.getSeqSortSecond())
                .orElseThrow(() -> new IllegalArgumentException("해당 중분류가 존재하지 않습니다."));

        Book updatedBook = Book.builder()
                .seqBook(bookId)
                .seqSortSecond(sortSecond)
                .isbnBook(existingBook.getIsbnBook())
                .titleBook(existingBook.getTitleBook())
                .authorBook(existingBook.getAuthorBook())
                .publisherBook(existingBook.getPublisherBook())
                .publishDateBook(existingBook.getPublishDateBook())
                .barcodeBook(bookSortAndBarcodeRequestDto.getBarcodeBook())
                .cntBook(bookSortAndBarcodeRequestDto.getCntBook())
                .printCheckBook(bookSortAndBarcodeRequestDto.isPrintCheckBook())
                .build();

        Book changedBook = bookDAO.updateBook(updatedBook);

        BookResponseDto bookResponseDto = convertToDto(changedBook);

        return bookResponseDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBookById(int bookId) throws Exception {
        Book selectedBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("삭제에 실패했습니다. 해당 도서는 존재하지 않습니다."));

        bookDAO.deleteBook(selectedBook);
    }

    @Override
    @Transactional
    public BookCountResponseDto getBookCount(String isbn) throws Exception {
        Integer counts = bookRepository.countByIsbnBook(isbn);
        return new BookCountResponseDto(isbn, counts);
    }

    // 연관검색어
    @Override
    public List<BookSearchResponseDto> searchBookTitles(String titleBook) throws Exception {
        List<Book> bookList = bookDAO.searchBooksRelated(titleBook);

        List<BookSearchResponseDto> responseList = new java.util.ArrayList<>();
        for (Book book : bookList) {
            BookSearchResponseDto bookSearchResponseDto = new BookSearchResponseDto(
                book.getTitleBook(),
                book.isPrintCheckBook()
                );
            responseList.add(bookSearchResponseDto);
        }

        return responseList;
    }

    // 정확한 제목 검색
    @Override
    public BookListResponseDto searchBooksByExactTitle(String titleBook) throws Exception {
        List<Book> books = bookDAO.searchBooksResultExact(titleBook);

        List<BookResponseDto> content = books.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int totalCount = content.size();
        return new BookListResponseDto(content, totalCount);
    }

    // 제목 포함 검색
    @Override
    public BookListResponseDto searchBooksByTitleContaining(String titleBook) throws Exception {
        List<Book> books = bookDAO.searchBooksResultContaining(titleBook);

        List<BookResponseDto> content = books.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int totalCount = content.size();
        return new BookListResponseDto(content, totalCount);
    }

    @Override
    @Transactional
    public void markBooksAsPrinted(List<Integer> bookIds) throws Exception {
        bookDAO.printPost(bookIds);
    }

    @Override
    public List<BookUnprintedResponseDto> findUnprintedBooks() throws Exception {
                
        return bookDAO.findUnprintedBooks().stream()
            .map(book -> new BookUnprintedResponseDto(
                book.getSeqBook(),
                book.getSeqSortSecond().getSeqSortSecond(),
                book.getCntBook(),
                book.getBarcodeBook(),
                book.getTitleBook()))
            .collect(Collectors.toList());
    }

    @Override
    public BookBarcodeUniqueResponseDto checkDuplicated(BookBarcodeUniqueRequestDto bookBarcodeUniqueRequestDto) throws Exception {
        Integer seqBook = bookBarcodeUniqueRequestDto.getSeqBook();
        String barcodeBook = bookBarcodeUniqueRequestDto.getBarcodeBook();
        
        boolean isDuplicated = bookDAO.checkDuplicates(seqBook, barcodeBook);

        String message;
        if (isDuplicated) {
            message = "이미 등록된 바코드입니다.";
        } else {
            message = "사용 가능한 바코드입니다.";
        }

        return new BookBarcodeUniqueResponseDto(isDuplicated, message);
    }

}
