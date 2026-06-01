package playbook.encore.back.book.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.book.service.BookService;

import playbook.encore.back.common.excel.ExcelUtil;
import playbook.encore.back.common.util.AuthUtil;

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
    public ResponseEntity<Response> getBooks(
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

        String mappedSortBy = mapSortField(sortBy);
        BookListResponseDto bookListResponseDto = bookService.getBookListWithPagination(idUser, campusId, page, size, mappedSortBy, sortDir);
        return ResponseEntity.ok(ResponseHandler.success(bookListResponseDto));
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
    public ResponseEntity<Response> getAllBooks(HttpServletRequest request) throws Exception {
        AuthUtil.requireAdmin(request);
        Integer campusId = AuthUtil.getCampusId(request, null);
        List<BookResponseDto> booklist = bookService.getAllBooks(campusId);
        return ResponseEntity.ok(ResponseHandler.success(booklist));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getBookById(
            HttpServletRequest request,
            @PathVariable("id") int bookId
    ) throws Exception {
        String idUser = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            idUser = (String) session.getAttribute("userId");
        }
        BookResponseDto bookResponseDto = bookService.getBookById(bookId, idUser);
        return ResponseEntity.ok(ResponseHandler.success(bookResponseDto));
    }

    @GetMapping("/sortFirst")
    public ResponseEntity<Response> getBooksBySortFirstId(
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
        return ResponseEntity.ok(ResponseHandler.success(bookListResponseDto));
    }

    @PostMapping
    public ResponseEntity<Response> insertBook(
            HttpServletRequest request,
            @RequestBody @Valid BookRequestDto bookRequestDto
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        BookResponseDto bookResponseDto = bookService.insertBook(bookRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseHandler.success(bookResponseDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateBookById(
            HttpServletRequest request,
            @PathVariable("id") int bookId,
            @RequestBody @Valid BookSortAndBarcodeRequestDto bookSortAndBarcodeRequestDto
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        BookResponseDto bookResponseDto = bookService.changeBook(bookId, bookSortAndBarcodeRequestDto);
        return ResponseEntity.ok(ResponseHandler.success(bookResponseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteBookById(
            HttpServletRequest request,
            @PathVariable("id") int bookId
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        bookService.deleteBookById(bookId);
        return ResponseEntity.ok(ResponseHandler.success());
    }

    @GetMapping("/count")
    public ResponseEntity<Response> getBookCountByIsbn(
            HttpServletRequest request,
            @RequestParam("isbn") String isbn
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        BookCountResponseDto bookCountResponseDto = bookService.getBookCount(isbn);
        return ResponseEntity.ok(ResponseHandler.success(bookCountResponseDto));
    }

    @GetMapping("/related")
    public ResponseEntity<Response> getBookTitleSimiler(
            HttpServletRequest request,
            @RequestParam("q") String query) throws Exception {
        Integer campusId = resolveCampusId(request);
        List<BookSearchResponseDto> bookSearchList = bookService.searchBookTitles(query, campusId);
        return ResponseEntity.ok(ResponseHandler.success(bookSearchList));
    }

    @GetMapping("/search")
    public ResponseEntity<Response> getSearchResults(
            HttpServletRequest request,
            @RequestParam("q") String query,
            @RequestParam(value = "exact", defaultValue = "false") boolean exact) throws Exception {
        Integer campusId = resolveCampusId(request);
        BookListResponseDto result = exact
                ? bookService.searchBooksByExactTitle(query, campusId)
                : bookService.searchBooksByTitleContaining(query, campusId);
        return ResponseEntity.ok(ResponseHandler.success(result));
    }

    @PutMapping("/batch/print")
    public ResponseEntity<Response> batchPrint(
            HttpServletRequest request,
            @RequestBody List<Integer> bookIds
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        bookService.markBooksAsPrinted(bookIds);
        return ResponseEntity.ok(ResponseHandler.success());
    }

    @GetMapping("/unprinted")
    public ResponseEntity<Response> getUnprintedBooks(HttpServletRequest request) throws Exception {
        AuthUtil.requireAdmin(request);
        List<BookUnprintedResponseDto> books = bookService.findUnprintedBooks();
        return ResponseEntity.ok(ResponseHandler.success(books));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        Integer campusId = AuthUtil.getCampusId(request, requestCampusId);
        byte[] data = bookService.exportExcel(campusId);
        return ExcelUtil.toResponse(data, "도서목록");
    }

    @PostMapping("/check/barcode")
    public ResponseEntity<Response> isBarcodeDuplicated(
            HttpServletRequest request,
            @RequestBody @Valid BookBarcodeUniqueRequestDto bookBarcodeUniqueRequestDto
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        BookBarcodeUniqueResponseDto bookBarcodeUniqueResponseDto = bookService.checkDuplicated(bookBarcodeUniqueRequestDto);
        return ResponseEntity.ok(ResponseHandler.success(bookBarcodeUniqueResponseDto));
    }
}
