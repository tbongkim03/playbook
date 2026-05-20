package playbook.encore.back.history.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseCode;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.history.dto.HistoryBookResponseDto;
import playbook.encore.back.history.dto.PopularLabelDto;
import playbook.encore.back.history.dto.UserReadingRankDto;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.history.service.HistoryService;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    @Autowired
    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/book")
    public ResponseEntity<Response> getHistoryBook(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                Integer campusId = requestCampusId != null ? requestCampusId : (Integer) request.getAttribute("campusId");
                HistoryBookResponseDto result = historyService.getHistoryBooks(campusId);
                return ResponseEntity.ok(ResponseHandler.success(result));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @DeleteMapping("/book/{historyId}")
    public ResponseEntity<Response> deleteHistoryBook(
            HttpServletRequest request,
            @PathVariable int historyId
    ) throws Exception {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                historyService.deleteHistoryBook(historyId);
                return ResponseEntity.ok(ResponseHandler.success());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @PostMapping("/borrow")
    public ResponseEntity<Response> borrowBook(
            HttpServletRequest request,
            @RequestBody String barcodeBook
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }

        LoginCheckInterceptor.RoleType role = (LoginCheckInterceptor.RoleType) roleAttr;
        Object user;
        switch (role) {
            case USER -> user = request.getAttribute("user");
            case ADMIN -> user = request.getAttribute("admin");
            default -> {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
            }
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseHandler.notAuthenticated());
        }

        Integer campusId = (Integer) request.getAttribute("campusId");

        try {
            historyService.handleBookBorrow(user, barcodeBook, campusId);
            return ResponseEntity.ok(ResponseHandler.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @PutMapping("/return")
    public ResponseEntity<Response> returnBook(
            HttpServletRequest request,
            @RequestBody String barcodeBook
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }

        LoginCheckInterceptor.RoleType role = (LoginCheckInterceptor.RoleType) roleAttr;
        Object user;
        switch (role) {
            case USER -> user = request.getAttribute("user");
            case ADMIN -> user = request.getAttribute("admin");
            default -> {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
            }
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseHandler.notAuthenticated());
        }

        Integer campusId = (Integer) request.getAttribute("campusId");

        try {
            historyService.handleBookReturn(user, barcodeBook, campusId);
            return ResponseEntity.ok(ResponseHandler.success());
        } catch (IllegalArgumentException e) {
            // 상태는 OK, 메시지만 연체 or 잘못된 형식
            return ResponseEntity.ok(ResponseHandler.error(ResponseCode.FAIL_PROCESS, e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Response> getMyHistory(HttpServletRequest request) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || roleAttr != LoginCheckInterceptor.RoleType.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }

        BookUser user = (BookUser) request.getAttribute("user");

        try {
            HistoryBookResponseDto result = historyService.getMyHistory(user);
            return ResponseEntity.ok(ResponseHandler.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @GetMapping("/popular/first/{courseId}")
    public ResponseEntity<Response> getPopularFirstSortByCourse(
            HttpServletRequest request,
            @PathVariable int courseId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        try {
            List<PopularLabelDto> result = historyService.findPopularFirstSortByCourse(courseId);
            return ResponseEntity.ok(ResponseHandler.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @GetMapping("/popular/first")
    public ResponseEntity<Response> getPopularFirstSortAll(HttpServletRequest request) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        try {
            Integer campusId = (Integer) request.getAttribute("campusId");
            List<PopularLabelDto> result = historyService.findPopularFirstSortAll(campusId);
            return ResponseEntity.ok(ResponseHandler.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @GetMapping("/popular/second/{courseId}")
    public ResponseEntity<Response> getPopularSecondSortByCourse(
            HttpServletRequest request,
            @PathVariable int courseId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        try {
            List<PopularLabelDto> result = historyService.findPopularSecondSortByCourse(courseId);
            return ResponseEntity.ok(ResponseHandler.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @GetMapping("/popular/second")
    public ResponseEntity<Response> getPopularSecondSortAll(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        try {
            Integer campusId = requestCampusId != null ? requestCampusId : (Integer) request.getAttribute("campusId");
            List<PopularLabelDto> result = historyService.findPopularSecondSortAll(campusId);
            return ResponseEntity.ok(ResponseHandler.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @GetMapping("/rank/{courseId}")
    public ResponseEntity<Response> getUserReadingRankByCourse(
            HttpServletRequest request,
            @PathVariable int courseId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        try {
            List<UserReadingRankDto> result = historyService.findUserReadingRankByCourse(courseId);
            return ResponseEntity.ok(ResponseHandler.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @GetMapping("/rank")
    public ResponseEntity<Response> getUserReadingRankAll(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        try {
            Integer campusId = requestCampusId != null ? requestCampusId : (Integer) request.getAttribute("campusId");
            List<UserReadingRankDto> result = historyService.findUserReadingRankAll(campusId);
            return ResponseEntity.ok(ResponseHandler.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }
}
