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
import playbook.encore.back.book.entity.Book;

import java.io.IOException;
import java.util.List;

public interface BookService {
    byte[] exportExcel(Integer campusId) throws IOException;
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

}
