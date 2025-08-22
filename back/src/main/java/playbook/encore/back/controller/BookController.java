package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.BookService;

import playbook.encore.back.jwt.jwtUtil;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final jwtUtil jwtUtil;


    @Autowired
    public BookController(BookService bookService, jwtUtil jwtUtil) {
        this.bookService = bookService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<BookListResponseDto> getBooks(
            HttpServletRequest request,
            @RequestParam int page
    ) throws Exception {
        String idUser = null;

        try {
            // JWT 토큰 추출 시도
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                String reason = jwtUtil.validateAndGetReason(token);

                if (reason == null || reason.equals("VALID")) {
                    idUser = jwtUtil.getIdUserFromToken(token);
                }
            }
        } catch (Exception e) {
            System.out.println("JWT 토큰 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
        BookListResponseDto bookListResponseDto = bookService.getBookList(page, idUser);
        return ResponseEntity.status(HttpStatus.OK).body(bookListResponseDto);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBooks(
            HttpServletRequest request
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        }
        List<Book> booklist = bookService.getAllBooks();
        return ResponseEntity.status(HttpStatus.OK).body(booklist);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(
            HttpServletRequest request,
            @PathVariable("id") int bookId
    ) throws Exception {

        String idUser = null;

        try {
            // JWT 토큰 추출 시도
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                String reason = jwtUtil.validateAndGetReason(token);

                if (reason == null || reason.equals("VALID")) {
                    idUser = jwtUtil.getIdUserFromToken(token);
                }
            }
        } catch (Exception e) {
            System.out.println("JWT 토큰 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
        BookResponseDto bookResponseDto = bookService.getBookById(bookId, idUser);
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
            @RequestBody BookRequestDto bookRequestDto
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");

        // 디버깅용 로그 추가
        System.out.println("roleAttr: " + roleAttr);
        System.out.println("roleAttr type: " + (roleAttr != null ? roleAttr.getClass() : "null"));
        System.out.println("ADMIN enum: " + LoginCheckInterceptor.RoleType.ADMIN);
        System.out.println("equals result: " + LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr));

        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
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
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            BookResponseDto bookResponseDto = bookService.changeBook(bookId, bookSortAndBarcodeRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(bookResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBookById(
            HttpServletRequest request,
            @PathVariable("id") int bookId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            bookService.deleteBookById(bookId);
            return ResponseEntity.status(HttpStatus.OK).body("삭제를 수행하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @GetMapping("/count")
    public ResponseEntity<?> getBookCountByIsbn(
            HttpServletRequest request,
            @RequestParam("isbn") String isbn
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
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
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            bookService.markBooksAsPrinted(bookIds);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @GetMapping("/unprinted")
    public ResponseEntity<?> getUnprintedBooks(
            HttpServletRequest request
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
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
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            BookBarcodeUniqueResponseDto bookBarcodeUniqueResponseDto = bookService.checkDuplicated(bookBarcodeUniqueRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(bookBarcodeUniqueResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }
}
