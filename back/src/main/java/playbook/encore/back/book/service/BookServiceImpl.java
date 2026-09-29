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
import playbook.encore.back.book.dto.BookImportResultDto;
import org.springframework.web.multipart.MultipartFile;
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

        // 표지URL·라벨출력은 2026-08-04 추가. 이 둘이 없으면 내보내기→가져오기 왕복이 성립하지 않는다.
        //  · 표지URL : img_url_book 이 NOT NULL 이라 신규 등록 시 값이 필요하다
        //  · 라벨출력 : 실물 라벨을 붙였다는 기록이다. 빠뜨리면 이관 후 전권을 다시 출력하게 된다
        List<String> headers = Arrays.asList(
                "도서번호", "제목", "ISBN", "저자", "출판사", "출판일",
                "대분류", "중분류", "수량", "대출상태", "바코드", "표지URL", "라벨출력"
        );
        List<List<Object>> rows = books.stream().map(b -> Arrays.<Object>asList(
                b.getSeqBook(), b.getTitleBook(),
                b.getIsbnBook(),
                b.getAuthorBook(),
                b.getPublisherBook(),
                b.getPublishDateBook() != null ? b.getPublishDateBook().toString() : "-",
                b.getSeqSortSecond().getSeqSortFirst().getKorSortFirst(),
                b.getSeqSortSecond().getKorSortSecond(),
                b.getCntBook(),
                b.isBookBorrowed() ? "대출중" : "대출가능",
                b.getBarcodeBook() != null ? b.getBarcodeBook() : "-",
                b.getImgUrlBook() != null ? b.getImgUrlBook() : "",
                b.isPrintCheckBook() ? "출력됨" : "미출력"
        )).collect(Collectors.toList());

        Workbook wb = ExcelUtil.createWorkbook(headers, rows);
        return ExcelUtil.toResponse(wb, "도서목록").getBody();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public BookImportResultDto importExcel(MultipartFile file, Integer adminCampusId, boolean allowInsert)
            throws Exception {
        log.info("[BookService] 도서 엑셀 업로드 - campusId: {}, allowInsert: {}", adminCampusId, allowInsert);
        java.util.List<java.util.List<String>> rows;
        try (java.io.InputStream in = file.getInputStream()) {
            rows = ExcelUtil.readRows(in);
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("빈 엑셀 파일입니다.");
        }

        java.util.List<String> header = rows.get(0);
        java.util.Map<String, Integer> idx = new java.util.HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            idx.put(header.get(i).trim(), i);
        }
        Integer idCol = idx.get("도서번호");
        if (idCol == null) {
            throw new IllegalArgumentException("'도서번호' 열이 없습니다. 내보내기 양식을 사용해주세요.");
        }

        int inserted = 0, updated = 0, skipped = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 마법사 경로에서는 "업로드 시작 시점에 이미 있던 번호" 만 기존 도서로 본다.
        // 행을 처리하는 도중에 새로 발급된 번호가 엑셀의 옛 번호와 겹치면
        // 방금 등록한 다른 도서를 덮어쓰게 되기 때문이다.
        java.util.Set<Integer> preExisting = allowInsert
                ? importExistingIds(rows, idCol)
                : java.util.Collections.emptySet();

        for (int r = 1; r < rows.size(); r++) {
            java.util.List<String> row = rows.get(r);
            int excelRow = r + 1;
            String idStr = importCell(row, idCol);

            // 도서번호가 비었으면 신규 등록 — 단 allowInsert 가 켜졌을 때만이다.
            // 관리자 화면(기본 false)에서는 예전처럼 그냥 건너뛴다. 실제 신규 도서는 바코드로 등록하고,
            // 빈 행이 조용히 등록되면 실수로 중복 도서만 쌓이기 때문이다.
            if (idStr.isBlank()) {
                if (!allowInsert || isBlankRow(row)) { skipped++; continue; }
                try {
                    importInsert(row, idx, adminCampusId);
                    inserted++;
                } catch (java.time.format.DateTimeParseException e) {
                    // DateTimeParseException 은 IllegalArgumentException 계열이 아니라 별개다
                    errors.add(excelRow + "행: 출판일 형식 오류(yyyy-MM-dd 필요)");
                    skipped++;
                } catch (NumberFormatException e) {
                    // NumberFormatException 은 IllegalArgumentException 의 하위 —
                    // 반드시 먼저 잡아야 한다 (뒤에 두면 도달 불가로 컴파일 실패)
                    errors.add(excelRow + "행: 수량 형식 오류");
                    skipped++;
                } catch (IllegalArgumentException e) {
                    errors.add(excelRow + "행: " + e.getMessage());
                    skipped++;
                }
                continue;
            }
            int seqBook;
            try {
                seqBook = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException e) {
                errors.add(excelRow + "행: 도서번호 형식 오류(" + idStr + ")");
                skipped++; continue;
            }
            Book book = allowInsert && !preExisting.contains(seqBook)
                    ? null
                    : bookRepository.findById(seqBook).orElse(null);
            if (book == null) {
                if (!allowInsert) {
                    errors.add(excelRow + "행: 존재하지 않는 도서번호(" + seqBook + ")");
                    skipped++; continue;
                }
                // 마법사 경로: 다른 서버에서 내보낸 파일을 그대로 올리면 도서번호가 전부 채워져 있다.
                // 이 DB 에 없는 번호는 이관 대상으로 보고 새 번호로 신규 등록한다 (옛 번호는 쓰지 않는다).
                try {
                    importInsert(row, idx, adminCampusId);
                    inserted++;
                } catch (java.time.format.DateTimeParseException e) {
                    errors.add(excelRow + "행: 출판일 형식 오류(yyyy-MM-dd 필요)");
                    skipped++;
                } catch (NumberFormatException e) {
                    errors.add(excelRow + "행: 수량 형식 오류");
                    skipped++;
                } catch (IllegalArgumentException e) {
                    errors.add(excelRow + "행: " + e.getMessage());
                    skipped++;
                }
                continue;
            }
            // 마법사 경로에서 번호가 이미 있어도 같은 책이라는 보장은 없다 — 이관을 마친 뒤
            // 옛 서버 파일을 다시 올리면 옛 3번과 새 3번은 다른 책이다. ISBN 이 다르면 덮어쓰지 않는다.
            if (allowInsert) {
                String isbn = importValue(row, idx, "ISBN").trim();
                if (!isbn.isBlank() && !"-".equals(isbn) && !isbn.equals(book.getIsbnBook())) {
                    errors.add(excelRow + "행: 도서번호 " + seqBook + " 는 이미 다른 도서(ISBN "
                            + book.getIsbnBook() + ")라 덮어쓰지 않았습니다. 새로 등록하려면 도서번호를 비우세요");
                    skipped++; continue;
                }
            }
            if (adminCampusId != null && book.getSeqCampus() != null
                    && !adminCampusId.equals(book.getSeqCampus().getSeqCampus())) {
                errors.add(excelRow + "행: 다른 캠퍼스 도서라 수정 권한이 없습니다(도서번호 " + seqBook + ")");
                skipped++; continue;
            }

            try {
                importApply(row, idx, "제목", 255, book::setTitleBook);
                importApply(row, idx, "ISBN", 20, book::setIsbnBook);
                importApply(row, idx, "저자", 20, book::setAuthorBook);
                importApply(row, idx, "출판사", 20, book::setPublisherBook);
                importApply(row, idx, "바코드", 30, book::setBarcodeBook);
                importApply(row, idx, "표지URL", 255, book::setImgUrlBook);

                // 라벨출력은 실물 라벨 부착 기록이다. 열이 없으면 건드리지 않는다
                // (구 양식으로 올렸을 때 기존 상태를 지우면 전권 재출력이 된다).
                String printed = importValue(row, idx, "라벨출력");
                if (!printed.isBlank() && !"-".equals(printed)) {
                    book.setPrintCheckBook(parsePrinted(printed));
                }

                String pub = importValue(row, idx, "출판일");
                if (!pub.isBlank() && !"-".equals(pub)) {
                    book.setPublishDateBook(java.time.LocalDate.parse(pub.trim()));
                }
                String cntStr = importValue(row, idx, "수량");
                if (!cntStr.isBlank() && !"-".equals(cntStr)) {
                    book.setCntBook((int) Double.parseDouble(cntStr.trim()));
                }
                String sortName = importValue(row, idx, "중분류");
                if (!sortName.isBlank() && !"-".equals(sortName)) {
                    java.util.List<SortSecond> found = sortSecondRepository.findByKorSortSecond(sortName.trim());
                    if (found.isEmpty()) {
                        errors.add(excelRow + "행: 중분류 '" + sortName + "'를 찾을 수 없습니다");
                        skipped++; continue;
                    }
                    book.setSeqSortSecond(found.get(0));
                }

                bookRepository.save(book);
                updated++;
            } catch (java.time.format.DateTimeParseException e) {
                errors.add(excelRow + "행: 출판일 형식 오류(yyyy-MM-dd 필요)");
                skipped++;
            } catch (NumberFormatException e) {
                errors.add(excelRow + "행: 수량 형식 오류");
                skipped++;
            } catch (IllegalArgumentException e) {
                errors.add(excelRow + "행: " + e.getMessage());
                skipped++;
            }
        }

        log.info("[BookService] 도서 업로드 완료 - 신규 {}, 갱신 {}, 스킵 {}", inserted, updated, skipped);
        return BookImportResultDto.builder()
                .inserted(inserted).updated(updated).skipped(skipped).errors(errors).build();
    }

    /** 엑셀에 적힌 도서번호 중 지금 DB 에 있는 번호. 숫자가 아닌 칸은 무시한다(행 처리에서 오류로 잡힌다). */
    private java.util.Set<Integer> importExistingIds(java.util.List<java.util.List<String>> rows, int idCol) {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (int r = 1; r < rows.size(); r++) {
            String v = importCell(rows.get(r), idCol).trim();
            if (v.isEmpty()) continue;
            try {
                ids.add(Integer.parseInt(v));
            } catch (NumberFormatException ignored) {
                // 행 처리 단계에서 "도서번호 형식 오류" 로 보고된다
            }
        }
        java.util.Set<Integer> existing = new java.util.HashSet<>();
        bookRepository.findAllById(ids).forEach(b -> existing.add(b.getSeqBook()));
        return existing;
    }

    /** 도서번호 외 모든 칸이 비었으면 빈 행으로 본다 (엑셀 하단의 잔여 행 무시). */
    private boolean isBlankRow(java.util.List<String> row) {
        for (String c : row) {
            if (c != null && !c.isBlank()) return false;
        }
        return true;
    }

    /** "출력됨"/"Y"/"1"/"true"/"O" 를 모두 출력 완료로 읽는다. 운영자가 손으로 채우는 칸이다. */
    private boolean parsePrinted(String v) {
        String s = v.trim();
        return s.equalsIgnoreCase("Y") || s.equals("1") || s.equalsIgnoreCase("true")
                || s.equalsIgnoreCase("O") || s.contains("출력됨") || s.equals("출력");
    }

    /** 필수 칸을 읽고 비었으면 어떤 칸인지 알려준다. */
    private String importRequired(java.util.List<String> row, java.util.Map<String, Integer> idx,
                                  String col, int maxLen) {
        String v = importValue(row, idx, col);
        if (v.isBlank() || "-".equals(v)) {
            throw new IllegalArgumentException("'" + col + "' 은(는) 신규 등록 시 필수입니다");
        }
        v = v.trim();
        if (v.length() > maxLen) {
            throw new IllegalArgumentException(col + " 길이 초과(최대 " + maxLen + "자)");
        }
        return v;
    }

    /**
     * 도서번호가 빈 행 → 신규 등록.
     *
     * <p>캠퍼스 결정 규칙
     * <ul>
     *   <li>캠퍼스 관리자 : 자기 캠퍼스로 강제 (엑셀로 타 캠퍼스에 밀어 넣지 못하게)</li>
     *   <li>전체관리자 : 캠퍼스가 하나뿐이면 그 캠퍼스. 여럿이면 거부 —
     *       설치 마법사의 초기 데이터 주입이 이 경로를 탄다(캠퍼스 1개인 신규 설치)</li>
     * </ul>
     */
    private void importInsert(java.util.List<String> row, java.util.Map<String, Integer> idx,
                              Integer adminCampusId) {
        Campus campus;
        if (adminCampusId != null) {
            campus = campusRepository.findById(adminCampusId)
                    .orElseThrow(() -> new IllegalArgumentException("소속 캠퍼스를 찾을 수 없습니다"));
        } else {
            java.util.List<Campus> all = campusRepository.findAll();
            if (all.size() == 1) {
                campus = all.get(0);
            } else if (all.isEmpty()) {
                throw new IllegalArgumentException("등록된 캠퍼스가 없습니다. 캠퍼스를 먼저 만드세요");
            } else {
                throw new IllegalArgumentException(
                        "캠퍼스가 " + all.size() + "개라 신규 등록 대상을 정할 수 없습니다. "
                                + "해당 캠퍼스 관리자 계정으로 업로드하세요");
            }
        }

        String sortName = importRequired(row, idx, "중분류", 50);
        java.util.List<SortSecond> found = sortSecondRepository.findByKorSortSecond(sortName);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("중분류 '" + sortName + "' 를 찾을 수 없습니다");
        }

        String cntStr = importValue(row, idx, "수량");
        int cnt = (cntStr.isBlank() || "-".equals(cntStr)) ? 1 : (int) Double.parseDouble(cntStr.trim());

        String barcode = importValue(row, idx, "바코드");
        String imgUrl = importValue(row, idx, "표지URL");
        String printed = importValue(row, idx, "라벨출력");

        Book book = Book.builder()
                .seqCampus(campus)
                .seqSortSecond(found.get(0))
                .titleBook(importRequired(row, idx, "제목", 255))
                .isbnBook(importRequired(row, idx, "ISBN", 20))
                .authorBook(importRequired(row, idx, "저자", 20))
                .publisherBook(importRequired(row, idx, "출판사", 20))
                .publishDateBook(java.time.LocalDate.parse(importRequired(row, idx, "출판일", 10)))
                // NOT NULL 이라 빈 값도 허용하되 null 은 안 된다. 표지 없이 등록하는 경우가 있다.
                .imgUrlBook(imgUrl.isBlank() || "-".equals(imgUrl) ? "" : imgUrl.trim())
                .barcodeBook(barcode.isBlank() || "-".equals(barcode) ? null : barcode.trim())
                .cntBook(cnt)
                .printCheckBook(!printed.isBlank() && !"-".equals(printed) && parsePrinted(printed))
                // 대여 상태는 절대 가져오지 않는다. 대여이력 없이 "대여중" 이면 반납이 불가능한 유령 상태가 된다.
                .isBookBorrowed(false)
                .build();
        bookRepository.save(book);
    }

    private String importCell(java.util.List<String> row, int i) {
        return (i >= 0 && i < row.size() && row.get(i) != null) ? row.get(i) : "";
    }

    private String importValue(java.util.List<String> row, java.util.Map<String, Integer> idx, String col) {
        Integer i = idx.get(col);
        return i != null ? importCell(row, i) : "";
    }

    private void importApply(java.util.List<String> row, java.util.Map<String, Integer> idx, String col,
                             int maxLen, java.util.function.Consumer<String> setter) {
        String v = importValue(row, idx, col);
        if (v.isBlank() || "-".equals(v)) {
            return;
        }
        v = v.trim();
        if (v.length() > maxLen) {
            throw new IllegalArgumentException(col + " 길이 초과(최대 " + maxLen + "자)");
        }
        setter.accept(v);
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
