package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dto.book.BookBarcodeUniqueRequestDto;
import playbook.encore.back.data.dto.book.BookBarcodeUniqueResponseDto;
import playbook.encore.back.data.dto.book.BookCountResponseDto;
import playbook.encore.back.data.dto.book.BookListResponseDto;
import playbook.encore.back.data.dto.book.BookRequestDto;
import playbook.encore.back.data.dto.book.BookResponseDto;
import playbook.encore.back.data.dto.book.BookSearchResponseDto;
import playbook.encore.back.data.dto.book.BookSortAndBarcodeRequestDto;
import playbook.encore.back.data.dto.book.BookUnprintedResponseDto;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;


    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<BookListResponseDto> getBooks(@RequestParam int page) throws Exception {
        BookListResponseDto bookListResponseDto = bookService.getBookList(page);
        return ResponseEntity.status(HttpStatus.OK).body(bookListResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable("id") int bookId) throws Exception {
        BookResponseDto bookResponseDto = bookService.getBookById(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(bookResponseDto);
    }

    @GetMapping("/sortFirst")
    public ResponseEntity<BookListResponseDto> getBooksBySortFirstId(
            @RequestParam("id") int sortFirstId,
            @RequestParam("page") int page
    ) throws Exception {
        BookListResponseDto bookListResponseDto = bookService.getBookListBySortFirst(sortFirstId, page);
        return ResponseEntity.status(HttpStatus.OK).body(bookListResponseDto);
    }

    @PostMapping
    public ResponseEntity<?> insertBook(
            HttpServletRequest request,
            @RequestBody  BookRequestDto bookRequestDto
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            BookResponseDto bookResponseDto = bookService.insertBook(bookRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(bookResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBookById(
            HttpServletRequest request,
            @PathVariable("id") int bookId,
            @RequestBody BookSortAndBarcodeRequestDto bookSortAndBarcodeRequestDto
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            BookResponseDto bookResponseDto = bookService.changeBook(bookId, bookSortAndBarcodeRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(bookResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBookById(@PathVariable("id") int bookId) throws Exception {
        bookService.deleteBookById(bookId);
        return ResponseEntity.status(HttpStatus.OK).body("삭제를 수행하였습니다.");
    }

    @GetMapping("/count")
    public ResponseEntity<?> getBookCountByIsbn(
            HttpServletRequest request,
            @RequestParam("isbn") String isbn
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            BookCountResponseDto bookCountResponseDto = bookService.getBookCount(isbn);
            return ResponseEntity.status(HttpStatus.OK).body(bookCountResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @GetMapping("/related")
    public ResponseEntity<List<BookSearchResponseDto>> getBookTitleSimiler(@RequestParam("q") String query) throws Exception {
        List<BookSearchResponseDto> bookSearchList = bookService.searchBookTitles(query);
        return ResponseEntity.status(HttpStatus.OK).body(bookSearchList);
    }

    @GetMapping("/search")
    public ResponseEntity<BookListResponseDto> getSearchResults(
        @RequestParam("q") String query, 
        @RequestParam(value = "exact", defaultValue = "false") boolean exact) throws Exception 
        {
            BookListResponseDto result = exact
            ? bookService.searchBooksByExactTitle(query)
            : bookService.searchBooksByTitleContaining(query);
            return ResponseEntity.ok(result);
    }

    @PutMapping("/batch/print")
    public ResponseEntity<?> batchPrint(
            HttpServletRequest request,
            @RequestBody List<Integer> bookIds
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            bookService.markBooksAsPrinted(bookIds);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @GetMapping("/unprinted")
    public ResponseEntity<?> getUnprintedBooks(
            HttpServletRequest request
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            List<BookUnprintedResponseDto> books = bookService.findUnprintedBooks();
            return ResponseEntity.ok(books);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @PostMapping("/check/barcode")
    public ResponseEntity<?> isBarcodeDuplicated(
            HttpServletRequest request,
            @RequestBody BookBarcodeUniqueRequestDto bookBarcodeUniqueRequestDto
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            BookBarcodeUniqueResponseDto bookBarcodeUniqueResponseDto = bookService.checkDuplicated(bookBarcodeUniqueRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(bookBarcodeUniqueResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }
}
