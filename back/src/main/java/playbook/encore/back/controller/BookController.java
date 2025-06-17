package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dao.BookDAO;
import playbook.encore.back.data.dto.book.BookRequestDto;
import playbook.encore.back.data.dto.book.BookResponseDto;
import playbook.encore.back.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final BookDAO bookDAO;

    @Autowired
    public BookController(BookService bookService, BookDAO bookDAO) {
        this.bookService = bookService;
        this.bookDAO = bookDAO;
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getBookAll() throws Exception {
        List<BookResponseDto> bookResponseDtoList = bookService.getAllBook();
        return ResponseEntity.status(HttpStatus.OK).body(bookResponseDtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable("id") Integer bookId) throws Exception {
        BookResponseDto bookResponseDto = bookService.getBookById(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(bookResponseDto);
    }

    @PostMapping
    public ResponseEntity<BookResponseDto> insertBook(@RequestBody  BookRequestDto bookRequestDto) throws Exception {
        BookResponseDto bookResponseDto = bookService.insertBook(bookRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBookById(
            @PathVariable("id") Integer bookId,
            @RequestBody BookRequestDto bookRequestDto
    ) throws Exception {
        bookRequestDto.setSeqBook(bookId);
        BookResponseDto bookResponseDto = bookService.changeBook(bookRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(bookResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBookById(@PathVariable("id") Integer bookId) throws Exception {
        bookService.deleteBookById(bookId);
        return ResponseEntity.status(HttpStatus.OK).body("삭제를 수행하였습니다.");
    }
}
