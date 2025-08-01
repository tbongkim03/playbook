package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.HistoryService;

@RestController
@RequestMapping("/api")
public class HistoryController {

    private final HistoryService historyService;

    @Autowired
    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    // 대여 부분
    @PutMapping("/borrow")
    public ResponseEntity<?> borrowBook(
            HttpServletRequest request,
            @RequestBody String barcodeBook
    ) throws Exception {
        Object user;
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
        }
        LoginCheckInterceptor.RoleType role = (LoginCheckInterceptor.RoleType) roleAttr;
        switch (role) {
            case USER -> user = request.getAttribute("user");
            case ADMIN -> user = request.getAttribute("admin");
            default -> {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("올바르지 않은 사용자입니다.");
            }
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
        }
        try {
            historyService.handleBookBorrow(user, barcodeBook);
            return ResponseEntity.status(HttpStatus.OK).body("도서 대출이 완료되었습니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }

    // 반납 부분
    @PutMapping("/return")
    public ResponseEntity<?> returnBook(
            HttpServletRequest request,
            @RequestBody String barcodeBook
    ) throws Exception {
        Object user;
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
        }
        LoginCheckInterceptor.RoleType role = (LoginCheckInterceptor.RoleType) roleAttr;
        switch (role) {
            case USER -> user = request.getAttribute("user");
            case ADMIN -> user = request.getAttribute("admin");
            default -> {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("올바르지 않은 사용자입니다.");
            }
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
        }
        try {
            historyService.handleBookReturn(user, barcodeBook);
            return ResponseEntity.status(HttpStatus.OK).body("도서 대출이 완료되었습니다.");
        } catch (Exception IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IllegalArgumentException.getMessage());
        }
    }
}
