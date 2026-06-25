package playbook.encore.back.book.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.book.dao.BookDAO;
import playbook.encore.back.history.dao.HistoryDAO;
import playbook.encore.back.book.dto.BookBarcodeUniqueRequestDto;
import playbook.encore.back.book.dto.BookBarcodeUniqueResponseDto;
import playbook.encore.back.book.dto.BookCountResponseDto;
import playbook.encore.back.book.dto.BookListResponseDto;
import playbook.encore.back.book.dto.BookRequestDto;
import playbook.encore.back.book.dto.BookResponseDto;
import playbook.encore.back.book.dto.BookSearchResponseDto;
import playbook.encore.back.book.dto.BookSortAndBarcodeRequestDto;
import playbook.encore.back.book.dto.BookUnprintedResponseDto;
import playbook.encore.back.book.entity.Book;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.sort.entity.SortSecond;
import playbook.encore.back.book.dao.BookRepository;
import playbook.encore.back.bookUser.dao.BookUserRepository;
import playbook.encore.back.campus.dao.CampusRepository;
import playbook.encore.back.sort.dao.SortSecondRepository;
import playbook.encore.back.book.service.BookService;

import playbook.encore.back.auditlog.annotation.AuditAction;
import playbook.encore.back.common.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final BookDAO bookDAO;
    private final BookRepository bookRepository;
    private final SortSecondRepository sortSecondRepository;
    private final HistoryDAO historyDAO;
    private final BookUserRepository bookUserRepository;
    private final playbook.encore.back.history.dao.HistoryRepository historyRepository;
    private final CampusRepository campusRepository;

    @Autowired
    public BookServiceImpl(BookDAO bookDAO, BookRepository bookRepository, SortSecondRepository sortSecondRepository, HistoryDAO historyDAO, BookUserRepository bookUserRepository, playbook.encore.back.history.dao.HistoryRepository historyRepository, CampusRepository campusRepository) {
        this.bookDAO = bookDAO;
        this.bookRepository = bookRepository;
        this.sortSecondRepository = sortSecondRepository;
        this.historyDAO = historyDAO;
        this.bookUserRepository = bookUserRepository;
        this.historyRepository = historyRepository;
        this.campusRepository = campusRepository;
    }

    private BookResponseDto convertToDto(Book entity) {
        int borrowCount = historyRepository.countBySeqBook(entity);
        return BookResponseDto.builder()
                .seqBook(entity.getSeqBook())
                .seqCampus(entity.getSeqCampus().getSeqCampus())
                .campusName(entity.getSeqCampus().getNameCampus())  // 캠퍼스 이름 추가
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
                .borrowCount(borrowCount)
                .build();
    }

    @Override
    @AuditAction(action = "BOOK_CREATE", targetType = "BOOK")
    @Transactional(rollbackFor = Exception.class)
    public BookResponseDto insertBook(BookRequestDto bookRequestDto) {
        log.info("[BookService] 도서 등록 - title: {}", bookRequestDto.getTitleBook());
        Campus campus = campusRepository.findById(bookRequestDto.getSeqCampus())
                .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스는 존재하지 않습니다"));

        SortSecond sortSecond = sortSecondRepository.findById(bookRequestDto.getSeqSortSecond())
                .orElseThrow(() -> new IllegalArgumentException("해당 분류가 존재하지 않습니다"));

        Book book = Book.builder()
                .seqCampus(campus)  // 캠퍼스 설정
                .seqSortSecond(sortSecond)
                .isbnBook(bookRequestDto.getIsbnBook())
                .titleBook(bookRequestDto.getTitleBook())
                .authorBook(bookRequestDto.getAuthorBook())
                .publisherBook(bookRequestDto.getPublisherBook())
                .publishDateBook(bookRequestDto.getPublishDateBook())
                .imgUrlBook(bookRequestDto.getImageBook())
                .barcodeBook(null)
                .cntBook(0)
                .printCheckBook(false)
                .build();

        Book savedBook = bookDAO.insertBook(book);
        BookResponseDto bookResponseDto = convertToDto(savedBook);

        return bookResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BookListResponseDto getBookList(String idUser, Integer campusId) throws Exception {
        log.info("[BookService] 도서 목록 조회 - idUser: {}, campusId: {}", idUser, campusId);
        // 캠퍼스별 도서 목록 조회
        List<Book> bookList;
        if (campusId != null) {
            bookList = bookRepository.findAllWithCategoriesByCampus(campusId);
        } else {
            bookList = bookDAO.selectBookListAll();  // 전체 관리자용
        }

        Integer userSeq = null;
        if (idUser != null) {
            userSeq = bookUserRepository.findByIdUser(idUser)
                    .map(BookUser::getSeqUser)
                    .orElse(null);
        }

        final Integer finalUserSeq = userSeq;

        List<BookResponseDto> content = bookList.stream()
                .map(book -> {
                    BookResponseDto bookResponseDto = convertToDto(book);

                    // 로그인한 사용자이고 userSeq가 존재하는 경우, 해당 책을 빌렸는지 확인
                    if (finalUserSeq != null) {
                        boolean isBorrowedByMe = checkIfBookBorrowedByUser(book.getSeqBook(), finalUserSeq);
                        bookResponseDto.setBorrowedByMe(isBorrowedByMe);
                    } else {
                        bookResponseDto.setBorrowedByMe(false);
                    }

                    return bookResponseDto;
                })
                .collect(Collectors.toList());

        int totalCount = (int) bookList.size();
        BookListResponseDto bookListResponseDto = new BookListResponseDto(content, totalCount);
        return bookListResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDto> getAllBooks(Integer campusId) throws Exception {
        log.info("[BookService] 전체 도서 조회 - campusId: {}", campusId);
        List<Book> books;
        if (campusId != null) {
            books = bookRepository.findAllWithCategoriesByCampus(campusId);
        } else {
            books = bookDAO.selectAllBooks();  // 전체 관리자용
        }
        List<BookResponseDto> booksDto = books.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return booksDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BookListResponseDto getBookListBySortFirst(int sortFirstId, Integer campusId) {
        log.info("[BookService] 대분류별 도서 목록 조회 - sortFirstId: {}, campusId: {}", sortFirstId, campusId);
        List<Book> books;

        if (campusId == null) {
            // 비로그인 사용자: 모든 캠퍼스 도서
            books = bookDAO.selectBookListBySortFirst(sortFirstId);
        } else {
            // 특정 캠퍼스 사용자: 해당 캠퍼스 도서만
            books = bookDAO.selectBookListBySortFirstAndCampus(sortFirstId, campusId);
        }

        List<BookResponseDto> content = books.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        int totalCount = content.size();
        BookListResponseDto bookListResponseDto = new BookListResponseDto(content, totalCount);
        return bookListResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BookListResponseDto getBookListWithPagination(String idUser, Integer campusId, int page, int size, String sortBy, String sortDir) throws Exception {
        log.info("[BookService] 도서 페이징 조회 - campusId: {}, page: {}, size: {}, sortBy: {}", campusId, page, size, sortBy);
        Page<Book> bookPage;
        
        if (campusId != null) {
            bookPage = bookDAO.selectBookListAllWithPaginationByCampus(campusId, page, size, sortBy, sortDir);
        } else {
            bookPage = bookDAO.selectBookListAllWithPagination(page, size, sortBy, sortDir);
        }

        Integer userSeq = null;
        if (idUser != null) {
            userSeq = bookUserRepository.findByIdUser(idUser)
                    .map(BookUser::getSeqUser)
                    .orElse(null);
        }

        final Integer finalUserSeq = userSeq;

        List<BookResponseDto> content = bookPage.getContent().stream()
                .map(book -> {
                    BookResponseDto bookResponseDto = convertToDto(book);

                    if (finalUserSeq != null) {
                        boolean isBorrowedByMe = checkIfBookBorrowedByUser(book.getSeqBook(), finalUserSeq);
                        bookResponseDto.setBorrowedByMe(isBorrowedByMe);
                    } else {
                        bookResponseDto.setBorrowedByMe(false);
                    }

                    return bookResponseDto;
                })
                .collect(Collectors.toList());

        // 인기순 정렬의 경우 borrowCount로 재정렬 필요
        if ("borrowCount".equals(sortBy)) {
            content.sort((a, b) -> {
                int compare = Integer.compare(b.getBorrowCount(), a.getBorrowCount());
                return sortDir.equalsIgnoreCase("desc") ? compare : -compare;
            });
        }

        int totalCount = (int) bookPage.getTotalElements();
        return new BookListResponseDto(content, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public BookListResponseDto getBookListBySortFirstWithPagination(int sortFirstId, Integer campusId, int page, int size, String sortBy, String sortDir) {
        log.info("[BookService] 대분류별 도서 페이징 조회 - sortFirstId: {}, campusId: {}, page: {}", sortFirstId, campusId, page);
        Page<Book> bookPage = bookDAO.selectBookListBySortFirstWithPagination(sortFirstId, campusId, page, size, sortBy, sortDir);

        List<BookResponseDto> content = bookPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 인기순 정렬의 경우 borrowCount로 재정렬 필요
        if ("borrowCount".equals(sortBy)) {
            content.sort((a, b) -> {
                int compare = Integer.compare(b.getBorrowCount(), a.getBorrowCount());
                return sortDir.equalsIgnoreCase("desc") ? compare : -compare;
            });
        }

        int totalCount = (int) bookPage.getTotalElements();
        return new BookListResponseDto(content, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public BookListResponseDto getBookListBySortSecondWithPagination(int sortSecondId, Integer campusId, int page, int size, String sortBy, String sortDir) {
        log.info("[BookService] 중분류별 도서 페이징 조회 - sortSecondId: {}, campusId: {}, page: {}", sortSecondId, campusId, page);
        Page<Book> bookPage = bookDAO.selectBookListBySortSecondWithPagination(sortSecondId, campusId, page, size, sortBy, sortDir);

        List<BookResponseDto> content = bookPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        if ("borrowCount".equals(sortBy)) {
            content.sort((a, b) -> {
                int compare = Integer.compare(b.getBorrowCount(), a.getBorrowCount());
                return sortDir.equalsIgnoreCase("desc") ? compare : -compare;
            });
        }

        int totalCount = (int) bookPage.getTotalElements();
        return new BookListResponseDto(content, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDto getBookById(int bookId, String idUser) throws Exception {
        log.info("[BookService] 도서 단건 조회 - bookId: {}", bookId);
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
    @AuditAction(action = "BOOK_UPDATE", targetType = "BOOK")
    @Transactional(rollbackFor = Exception.class)
    public BookResponseDto changeBook(int bookId, BookSortAndBarcodeRequestDto bookSortAndBarcodeRequestDto) throws Exception {
        log.info("[BookService] 도서 수정 - bookId: {}", bookId);
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
    @AuditAction(action = "BOOK_DELETE", targetType = "BOOK")
    @Transactional(rollbackFor = Exception.class)
    public void deleteBookById(int bookId) throws Exception {
        log.info("[BookService] 도서 삭제 - bookId: {}", bookId);
        Book selectedBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("삭제에 실패했습니다. 해당 도서는 존재하지 않습니다."));

        bookDAO.deleteBook(selectedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookCountResponseDto getBookCount(String isbn) throws Exception {
        log.info("[BookService] ISBN별 도서 수 조회 - isbn: {}", isbn);
        Integer counts = bookRepository.countByIsbnBook(isbn);
        return new BookCountResponseDto(isbn, counts);
    }

    // 연관검색어
    @Override
    @Transactional(readOnly = true)
    public List<BookSearchResponseDto> searchBookTitles(String titleBook, Integer campusId) throws Exception {
        log.info("[BookService] 도서 연관 검색 - query: {}, campusId: {}", titleBook, campusId);
        List<Book> bookList;

        if (campusId == null) {
            // 비로그인 사용자 또는 전체 관리자: 모든 캠퍼스 도서 검색
            bookList = bookDAO.searchBooksRelated(titleBook);
        } else {
            // 특정 캠퍼스 사용자/관리자: 해당 캠퍼스 도서만 검색
            bookList = bookDAO.searchBooksRelatedByCampus(titleBook, campusId);
        }

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
    @Transactional(readOnly = true)
    public BookListResponseDto searchBooksByExactTitle(String titleBook, Integer campusId) throws Exception {
        log.info("[BookService] 도서 정확한 제목 검색 - title: {}", titleBook);
        List<Book> books;

        if (campusId == null) {
            // 비로그인 사용자 또는 전체 관리자: 모든 캠퍼스 도서 검색
            books = bookDAO.searchBooksResultExact(titleBook);
        } else {
            // 특정 캠퍼스 사용자/관리자: 해당 캠퍼스 도서만 검색
            books = bookDAO.searchBooksResultExactByCampus(titleBook, campusId);
        }

        List<BookResponseDto> content = books.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int totalCount = content.size();
        return new BookListResponseDto(content, totalCount);
    }

    // 제목 포함 검색
    @Override
    @Transactional(readOnly = true)
    public BookListResponseDto searchBooksByTitleContaining(String titleBook, Integer campusId) throws Exception {
        log.info("[BookService] 도서 제목 포함 검색 - title: {}", titleBook);
        List<Book> books;

        if (campusId == null) {
            // 비로그인 사용자 또는 전체 관리자: 모든 캠퍼스 도서 검색
            books = bookDAO.searchBooksResultContaining(titleBook);
        } else {
            // 특정 캠퍼스 사용자/관리자: 해당 캠퍼스 도서만 검색
            books = bookDAO.searchBooksResultContainingByCampus(titleBook, campusId);
        }

        List<BookResponseDto> content = books.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int totalCount = content.size();
        return new BookListResponseDto(content, totalCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markBooksAsPrinted(List<Integer> bookIds) throws Exception {
        log.info("[BookService] 도서 인쇄 처리 - count: {}", bookIds.size());
        bookDAO.printPost(bookIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookUnprintedResponseDto> findUnprintedBooks() throws Exception {
        log.info("[BookService] 미인쇄 도서 조회");
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
    @Transactional(readOnly = true)
    public BookBarcodeUniqueResponseDto checkDuplicated(BookBarcodeUniqueRequestDto bookBarcodeUniqueRequestDto) throws Exception {
        log.info("[BookService] 바코드 중복 확인 - barcode: {}", bookBarcodeUniqueRequestDto.getBarcodeBook());
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

    @Override
    @Transactional(readOnly = true)
    public byte[] exportExcel(Integer campusId) throws IOException {
        log.info("[BookService] 도서 목록 엑셀 내보내기 - campusId: {}", campusId);
        List<Book> books = (campusId != null)
                ? bookRepository.findAllWithCategoriesByCampus(campusId)
                : bookRepository.findAllWithCategories();

        List<String> headers = Arrays.asList(
                "제목", "ISBN", "저자", "출판사", "출판일",
                "대분류", "중분류", "수량", "대출상태", "바코드"
        );
        List<List<Object>> rows = books.stream().map(b -> Arrays.<Object>asList(
                b.getTitleBook(),
                b.getIsbnBook(),
                b.getAuthorBook(),
                b.getPublisherBook(),
                b.getPublishDateBook() != null ? b.getPublishDateBook().toString() : "-",
                b.getSeqSortSecond().getSeqSortFirst().getKorSortFirst(),
                b.getSeqSortSecond().getKorSortSecond(),
                b.getCntBook(),
                b.isBookBorrowed() ? "대출중" : "대출가능",
                b.getBarcodeBook() != null ? b.getBarcodeBook() : "-"
        )).collect(Collectors.toList());

        Workbook wb = ExcelUtil.createWorkbook(headers, rows);
        return ExcelUtil.toResponse(wb, "도서목록").getBody();
    }

    @Override
    @Transactional(readOnly = true)
    public BookListResponseDto getAdminBookList(
            Integer campusId, String search, Integer seqSortFirst, Integer seqSortSecond,
            String borrowStatus, Integer registerYear, Integer registerMonth,
            java.time.LocalDate registerStartDate, java.time.LocalDate registerEndDate,
            int page, int size, String sortBy, String sortDir) {
        log.info("[BookService] 관리자 도서 검색 - campusId: {}, search: {}, page: {}", campusId, search, page);
        Page<Book> bookPage = bookDAO.selectAdminBookListWithFilters(
                campusId, search, seqSortFirst, seqSortSecond, borrowStatus,
                registerYear, registerMonth, registerStartDate, registerEndDate,
                page, size, sortBy, sortDir);
        List<BookResponseDto> content = bookPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new BookListResponseDto(content, (int) bookPage.getTotalElements());
    }
}
