package playbook.encore.back.book.service;

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
import playbook.encore.back.book.entity.Book;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BookService {
    byte[] exportExcel(Integer campusId) throws IOException;
    /** 엑셀 업로드: 도서번호(seq_book) 기준으로 기존 도서만 갱신(신규 추가·삭제 없음) */
    /**
     * 도서 엑셀 업로드.
     *
     * @param allowInsert 도서번호가 비었거나 이 DB 에 없는 행을 신규 등록할지 여부.
     *                    <b>기본은 false</b> — 관리자 화면에서는 "기존 도서 갱신" 전용이다.
     *                    빈 행이 조용히 신규 등록되면 실수로 중복 도서가 생기고, 실제 신규 도서는
     *                    바코드 스캔으로 등록하므로 웹에서는 이 경로가 필요 없다.
     *                    설치 마법사의 초기 장서 주입만 true 로 켠다.
     */
    BookImportResultDto importExcel(MultipartFile file, Integer adminCampusId, boolean allowInsert) throws Exception;
    BookResponseDto insertBook(BookRequestDto bookRequestDto);
    BookListResponseDto getBookList(String idUser, Integer campusId) throws Exception;
    BookListResponseDto getBookListWithPagination(String idUser, Integer campusId, int page, int size, String sortBy, String sortDir) throws Exception;
    List<BookResponseDto> getAllBooks(Integer campusId) throws Exception;
    BookResponseDto getBookById(int bookId, String idUser) throws Exception;
    BookResponseDto changeBook(int bookId, BookSortAndBarcodeRequestDto bookSortAndBarcodeRequestDto) throws Exception;
    void deleteBookById(int bookId) throws Exception;
    BookCountResponseDto getBookCount(String isbn) throws Exception;
    List<BookSearchResponseDto> searchBookTitles(String titleBook, Integer campusId) throws Exception;
    BookListResponseDto searchBooksByExactTitle(String titleBook, Integer campusId) throws Exception;
    BookListResponseDto searchBooksByTitleContaining(String titleBook, Integer campusId) throws Exception;
    void markBooksAsPrinted(List<Integer> bookIds) throws Exception;
    List<BookUnprintedResponseDto> findUnprintedBooks() throws Exception;
    BookBarcodeUniqueResponseDto checkDuplicated(BookBarcodeUniqueRequestDto bookBarcodeUniqueRequestDto) throws Exception;
    BookListResponseDto getBookListBySortFirst(int sortFirstId, Integer campusId);
    BookListResponseDto getBookListBySortFirstWithPagination(int sortFirstId, Integer campusId, int page, int size, String sortBy, String sortDir);

    BookListResponseDto getBookListBySortSecondWithPagination(int sortSecondId, Integer campusId, int page, int size, String sortBy, String sortDir);

    BookListResponseDto getAdminBookList(
            Integer campusId, String search, Integer seqSortFirst, Integer seqSortSecond,
            String borrowStatus, Integer registerYear, Integer registerMonth,
            java.time.LocalDate registerStartDate, java.time.LocalDate registerEndDate,
            int page, int size, String sortBy, String sortDir);
}
