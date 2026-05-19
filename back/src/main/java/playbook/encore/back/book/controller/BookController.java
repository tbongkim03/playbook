package playbook.encore.back.book.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.book.dto.BookBarcodeUniqueRequestDto;
import playbook.encore.back.book.dto.BookBarcodeUniqueResponseDto;
import playbook.encore.back.book.dto.BookCountResponseDto;
import playbook.encore.back.book.dto.BookListResponseDto;
import playbook.encore.back.book.dto.BookRequestDto;
import playbook.encore.back.book.dto.BookResponseDto;
import playbook.encore.back.book.dto.BookSearchResponseDto;
import playbook.encore.back.book.dto.BookSortAndBarcodeRequestDto;
import playbook.encore.back.book.dto.BookUnprintedResponseDto;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.bookUser.dao.BookUserRepository;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.book.service.BookService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final BookUserRepository bookUserRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public BookController(BookService bookService, BookUserRepository bookUserRepository, AdminRepository adminRepository) {
        this.bookService = bookService;
        this.bookUserRepository = bookUserRepository;
        this.adminRepository = adminRepository;
    }

    private Integer resolveCampusId(HttpServletRequest request) {
        // 인터셉터가 이미 설정한 campusId 우선 사용
        Integer campusId = (Integer) request.getAttribute("campusId");
        if (campusId != null) {
            return campusId;
        }
        // 세션에서 userId/role로 직접 계산
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) return null;
        if ("admin".equalsIgnoreCase(role)) {
            Optional<playbook.encore.back.admin.entity.Admin> adminOpt = adminRepository.findByIdAdminWithCampus(userId);
            if (adminOpt.isPresent() && adminOpt.get().getSeqCampus() != null) {
                return adminOpt.get().getSeqCampus().getSeqCampus();
            }
        } else if ("user".equalsIgnoreCase(role)) {
            Optional<playbook.encore.back.bookUser.entity.BookUser> userOpt = bookUserRepository.findByIdUserWithCourseAndCampus(userId);
            if (userOpt.isPresent() && userOpt.get().getSeqCourse() != null && userOpt.get().getSeqCourse().getSeqCampus() != null) {
                return userOpt.get().getSeqCourse().getSeqCampus().getSeqCampus();
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getBooks(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "seqBook") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        String idUser = null;
        Integer campusId = null;

        if (requestCampusId != null) {
            campusId = requestCampusId;
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                idUser = (String) session.getAttribute("userId");
                campusId = resolveCampusId(request);
            }
        }

        try {
            String mappedSortBy = mapSortField(sortBy);
            BookListResponseDto bookListResponseDto = bookService.getBookListWithPagination(idUser, campusId, page, size, mappedSortBy, sortDir);
            return ResponseEntity.status(HttpStatus.OK).body(bookListResponseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    private String mapSortField(String sortBy) {
        switch (sortBy) {
            case "latest":
                return "seqBook";
            case "title":
                return "titleBook";
            case "author":
                return "authorBook";
            case "popular":
                return "borrowCount";
            default:
                return "seqBook";
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBooks(HttpServletRequest request) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        }

        Integer campusId = (Integer) request.getAttribute("campusId");

        try {
            List<BookResponseDto> booklist = bookService.getAllBooks(campusId);
            return ResponseEntity.status(HttpStatus.OK).body(booklist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(
            HttpServletRequest request,
            @PathVariable("id") int bookId
    ) throws Exception {
        String idUser = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            idUser = (String) session.getAttribute("userId");
        }
        BookResponseDto bookResponseDto = bookService.getBookById(bookId, idUser);
        return ResponseEntity.status(HttpStatus.OK).body(bookResponseDto);
    }

    @GetMapping("/sortFirst")
    public ResponseEntity<BookListResponseDto> getBooksBySortFirstId(
            HttpServletRequest request,
            @RequestParam("id") int sortFirstId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "seqBook") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        Integer campusId = requestCampusId != null ? requestCampusId : resolveCampusId(request);
        String mappedSortBy = mapSortField(sortBy);
        BookListResponseDto bookListResponseDto = bookService.getBookListBySortFirstWithPagination(sortFirstId, campusId, page, size, mappedSortBy, sortDir);
        return ResponseEntity.status(HttpStatus.OK).body(bookListResponseDto);
    }

    @PostMapping
    public ResponseEntity<?> insertBook(
            HttpServletRequest request,
            @RequestBody BookRequestDto bookRequestDto
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
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
    public ResponseEntity<List<BookSearchResponseDto>> getBookTitleSimiler(
            HttpServletRequest request,
            @RequestParam("q") String query) throws Exception {
        Integer campusId = resolveCampusId(request);
        List<BookSearchResponseDto> bookSearchList = bookService.searchBookTitles(query, campusId);
        return ResponseEntity.status(HttpStatus.OK).body(bookSearchList);
    }

    @GetMapping("/search")
    public ResponseEntity<BookListResponseDto> getSearchResults(
            HttpServletRequest request,
            @RequestParam("q") String query,
            @RequestParam(value = "exact", defaultValue = "false") boolean exact) throws Exception {
        Integer campusId = resolveCampusId(request);
        BookListResponseDto result = exact
                ? bookService.searchBooksByExactTitle(query, campusId)
                : bookService.searchBooksByTitleContaining(query, campusId);
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
    public ResponseEntity<?> getUnprintedBooks(HttpServletRequest request) throws Exception {
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
