package playbook.encore.back.book.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import playbook.encore.back.book.entity.Book;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.bookUser.dao.BookUserRepository;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.book.service.BookService;

import playbook.encore.back.jwt.jwtUtil;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final jwtUtil jwtUtil;
    private final BookUserRepository bookUserRepository;
    private final AdminRepository adminRepository;


    @Autowired
    public BookController(BookService bookService, jwtUtil jwtUtil, BookUserRepository bookUserRepository, AdminRepository adminRepository) {
        this.bookService = bookService;
        this.jwtUtil = jwtUtil;
        this.bookUserRepository = bookUserRepository;
        this.adminRepository = adminRepository;
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

        // 쿼리 파라미터로 전달된 campusId가 있으면 우선 사용 (전체 관리자/비회원이 캠퍼스를 선택한 경우)
        if (requestCampusId != null) {
            campusId = requestCampusId;
        } else {
            // JWT 토큰 추출 시도 (토큰이 없어도 동작 가능)
            try {
                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    String reason = jwtUtil.validateAndGetReason(token);

                    if (reason == null || reason.equals("VALID")) {
                        idUser = jwtUtil.getIdUserFromToken(token);
                        String role = jwtUtil.getRoleFromToken(token);
                        
                        // Interceptor가 설정한 campusId 가져오기 시도
                        campusId = (Integer) request.getAttribute("campusId");
                        
                        // Interceptor가 실행되지 않은 경우 직접 계산
                        if (campusId == null) {
                            if ("admin".equalsIgnoreCase(role)) {
                                Optional<playbook.encore.back.admin.entity.Admin> adminOpt = adminRepository.findByIdAdminWithCampus(idUser);
                                if (adminOpt.isPresent() && adminOpt.get().getSeqCampus() != null) {
                                    campusId = adminOpt.get().getSeqCampus().getSeqCampus();
                                }
                            } else if ("user".equalsIgnoreCase(role)) {
                                Optional<playbook.encore.back.bookUser.entity.BookUser> userOpt = bookUserRepository.findByIdUserWithCourseAndCampus(idUser);
                                if (userOpt.isPresent() && userOpt.get().getSeqCourse() != null && userOpt.get().getSeqCourse().getSeqCampus() != null) {
                                    campusId = userOpt.get().getSeqCourse().getSeqCampus().getSeqCampus();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 토큰 처리 실패는 무시하고 계속 진행 (비로그인 사용자로 처리)
                System.out.println("JWT 토큰 처리 실패 (무시): " + e.getMessage());
            }
        }
        
        try {
            // 정렬 필드 매핑 (프론트엔드에서 사용하는 필드명을 백엔드 필드명으로 변환)
            String mappedSortBy = mapSortField(sortBy);
            
            BookListResponseDto bookListResponseDto = bookService.getBookListWithPagination(idUser, campusId, page, size, mappedSortBy, sortDir);
            return ResponseEntity.status(HttpStatus.OK).body(bookListResponseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
    
    // 정렬 필드 매핑 헬퍼 메서드
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
    public ResponseEntity<?> getAllBooks(
            HttpServletRequest request
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        }

        Integer campusId = (Integer) request.getAttribute("campusId");

        try {
            List<BookResponseDto> booklist = bookService.getAllBooks(campusId);
            return ResponseEntity.status(HttpStatus.OK).body(booklist);
        } catch (Exception e) {
            System.out.println("책 목록 조회 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
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
            HttpServletRequest request,
            @RequestParam("id") int sortFirstId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "seqBook") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        Integer campusId = null;

        // 쿼리 파라미터로 전달된 campusId가 있으면 우선 사용 (전체 관리자/비회원이 캠퍼스를 선택한 경우)
        if (requestCampusId != null) {
            campusId = requestCampusId;
        } else {
            // JWT 토큰이 있으면 campusId 계산
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String reason = jwtUtil.validateAndGetReason(token);
                    
                    if (reason == null || reason.equals("VALID")) {
                        String idUser = jwtUtil.getIdUserFromToken(token);
                        String role = jwtUtil.getRoleFromToken(token);
                        
                        // Interceptor가 설정한 campusId 가져오기 시도
                        campusId = (Integer) request.getAttribute("campusId");
                        
                        // Interceptor가 실행되지 않은 경우 직접 계산
                        if (campusId == null) {
                            if ("admin".equalsIgnoreCase(role)) {
                                Optional<playbook.encore.back.admin.entity.Admin> adminOpt = adminRepository.findByIdAdminWithCampus(idUser);
                                if (adminOpt.isPresent() && adminOpt.get().getSeqCampus() != null) {
                                    campusId = adminOpt.get().getSeqCampus().getSeqCampus();
                                }
                            } else if ("user".equalsIgnoreCase(role)) {
                                Optional<playbook.encore.back.bookUser.entity.BookUser> userOpt = bookUserRepository.findByIdUserWithCourseAndCampus(idUser);
                                if (userOpt.isPresent() && userOpt.get().getSeqCourse() != null && userOpt.get().getSeqCourse().getSeqCampus() != null) {
                                    campusId = userOpt.get().getSeqCourse().getSeqCampus().getSeqCampus();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // 토큰 검증 실패 시 campusId는 null로 유지 (비로그인 사용자)
                }
            }
        }

        // 정렬 필드 매핑
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

        // 디버깅용 로그 추가
        System.out.println("roleAttr: " + roleAttr);
        System.out.println("roleAttr type: " + (roleAttr != null ? roleAttr.getClass() : "null"));
        System.out.println("ADMIN enum: " + LoginCheckInterceptor.RoleType.ADMIN);
        System.out.println("equals result: " + LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr));

        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            Integer campusId = (Integer) request.getAttribute("campusId");
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
        Integer campusId = null;
        
        // JWT 토큰이 있으면 campusId 계산
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String reason = jwtUtil.validateAndGetReason(token);
                
                if (reason == null || reason.equals("VALID")) {
                    String idUser = jwtUtil.getIdUserFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);
                    
                    // Interceptor가 설정한 campusId 가져오기 시도
                    campusId = (Integer) request.getAttribute("campusId");
                    
                    // Interceptor가 실행되지 않은 경우 직접 계산
                    if (campusId == null) {
                        if ("admin".equalsIgnoreCase(role)) {
                            Optional<playbook.encore.back.admin.entity.Admin> adminOpt = adminRepository.findByIdAdminWithCampus(idUser);
                            if (adminOpt.isPresent() && adminOpt.get().getSeqCampus() != null) {
                                campusId = adminOpt.get().getSeqCampus().getSeqCampus();
                            }
                        } else if ("user".equalsIgnoreCase(role)) {
                            Optional<playbook.encore.back.bookUser.entity.BookUser> userOpt = bookUserRepository.findByIdUserWithCourseAndCampus(idUser);
                            if (userOpt.isPresent() && userOpt.get().getSeqCourse() != null && userOpt.get().getSeqCourse().getSeqCampus() != null) {
                                campusId = userOpt.get().getSeqCourse().getSeqCampus().getSeqCampus();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 토큰 검증 실패 시 campusId는 null로 유지 (비로그인 사용자)
            }
        }
        
        List<BookSearchResponseDto> bookSearchList = bookService.searchBookTitles(query, campusId);
        return ResponseEntity.status(HttpStatus.OK).body(bookSearchList);
    }

    @GetMapping("/search")
    public ResponseEntity<BookListResponseDto> getSearchResults(
        HttpServletRequest request,
        @RequestParam("q") String query,
        @RequestParam(value = "exact", defaultValue = "false") boolean exact) throws Exception
        {
            Integer campusId = null;
            
            // JWT 토큰이 있으면 campusId 계산
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String reason = jwtUtil.validateAndGetReason(token);
                    
                    if (reason == null || reason.equals("VALID")) {
                        String idUser = jwtUtil.getIdUserFromToken(token);
                        String role = jwtUtil.getRoleFromToken(token);
                        
                        // Interceptor가 설정한 campusId 가져오기 시도
                        campusId = (Integer) request.getAttribute("campusId");
                        
                        // Interceptor가 실행되지 않은 경우 직접 계산
                        if (campusId == null) {
                            if ("admin".equalsIgnoreCase(role)) {
                                Optional<playbook.encore.back.admin.entity.Admin> adminOpt = adminRepository.findByIdAdminWithCampus(idUser);
                                if (adminOpt.isPresent() && adminOpt.get().getSeqCampus() != null) {
                                    campusId = adminOpt.get().getSeqCampus().getSeqCampus();
                                }
                            } else if ("user".equalsIgnoreCase(role)) {
                                Optional<playbook.encore.back.bookUser.entity.BookUser> userOpt = bookUserRepository.findByIdUserWithCourseAndCampus(idUser);
                                if (userOpt.isPresent() && userOpt.get().getSeqCourse() != null && userOpt.get().getSeqCourse().getSeqCampus() != null) {
                                    campusId = userOpt.get().getSeqCourse().getSeqCampus().getSeqCampus();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // 토큰 검증 실패 시 campusId는 null로 유지 (비로그인 사용자)
                }
            }
            
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
