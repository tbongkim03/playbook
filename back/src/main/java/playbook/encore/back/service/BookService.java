package playbook.encore.back.service;

import playbook.encore.back.data.dto.book.BookRequestDto;
import playbook.encore.back.data.dto.book.BookResponseDto;

import java.util.List;

public interface BookService {
    BookResponseDto insertBook(BookRequestDto bookRequestDto);
    List<BookResponseDto> getAllBook() throws Exception;
    BookResponseDto getBookById(Integer bookId) throws Exception;
    BookResponseDto changeBook(BookRequestDto bookRequestDto) throws Exception;
    void deleteBookById(Integer bookId) throws Exception;

}
