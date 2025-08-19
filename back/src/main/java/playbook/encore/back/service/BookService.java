package playbook.encore.back.service;

import playbook.encore.back.data.dto.book.BookBarcodeUniqueRequestDto;
import playbook.encore.back.data.dto.book.BookBarcodeUniqueResponseDto;
import playbook.encore.back.data.dto.book.BookCountResponseDto;
import playbook.encore.back.data.dto.book.BookListResponseDto;
import playbook.encore.back.data.dto.book.BookRequestDto;
import playbook.encore.back.data.dto.book.BookResponseDto;
import playbook.encore.back.data.dto.book.BookSearchResponseDto;
import playbook.encore.back.data.dto.book.BookSortAndBarcodeRequestDto;
import playbook.encore.back.data.dto.book.BookUnprintedResponseDto;

import java.util.List;

public interface BookService {
    BookResponseDto insertBook(BookRequestDto bookRequestDto);
    BookListResponseDto getBookList(int page) throws Exception;
    BookListResponseDto getAllBooks(int page) throws Exception;
    BookResponseDto getBookById(int bookId, String idUser) throws Exception;
    BookResponseDto changeBook(int bookId, BookSortAndBarcodeRequestDto bookSortAndBarcodeRequestDto) throws Exception;
    void deleteBookById(int bookId) throws Exception;
    BookCountResponseDto getBookCount(String isbn) throws Exception;
    List<BookSearchResponseDto> searchBookTitles(String titleBook) throws Exception;
    BookListResponseDto searchBooksByExactTitle(String titleBook) throws Exception;
    BookListResponseDto searchBooksByTitleContaining(String titleBook) throws Exception;
    void markBooksAsPrinted(List<Integer> bookIds) throws Exception;
    List<BookUnprintedResponseDto> findUnprintedBooks() throws Exception;
    BookBarcodeUniqueResponseDto checkDuplicated(BookBarcodeUniqueRequestDto bookBarcodeUniqueRequestDto) throws Exception;
    BookListResponseDto getBookListBySortFirst(int sortFirstId, int page);

}
