package playbook.encore.back.service;

import playbook.encore.back.data.dto.book.BookCountResponseDto;
import playbook.encore.back.data.dto.book.BookListResponseDto;
import playbook.encore.back.data.dto.book.BookRequestDto;
import playbook.encore.back.data.dto.book.BookResponseDto;
import playbook.encore.back.data.dto.book.BookSearchResponseDto;

import java.util.List;

public interface BookService {
    BookResponseDto insertBook(BookRequestDto bookRequestDto);
    BookListResponseDto getBookList(int page) throws Exception;
    BookResponseDto getBookById(Integer bookId) throws Exception;
    BookResponseDto changeBook(Integer bookId, BookRequestDto bookRequestDto) throws Exception;
    void deleteBookById(Integer bookId) throws Exception;
    BookCountResponseDto getBookCount(String isbn) throws Exception;
    List<BookSearchResponseDto> searchBookTitles(String titleBook) throws Exception;
}
