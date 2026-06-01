package playbook.encore.back.favor.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.favor.dto.FavorResponseDto;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.favor.service.FavorService;

import java.util.List;

@RestController
@RequestMapping("/favor")
public class FavorController {
    private final FavorService favorService;

    @Autowired
    public FavorController(FavorService favorService) {
        this.favorService = favorService;
    }

    @GetMapping
    public ResponseEntity<Response> getFavor(HttpServletRequest request) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        Object userAttr = request.getAttribute("user");
        if (userAttr == null || !(userAttr instanceof BookUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.noSession("user"));
        }
        BookUser user = (BookUser) userAttr;
        List<FavorResponseDto> favorData = favorService.getFavorList(user);
        return ResponseEntity.ok(ResponseHandler.success(favorData));
    }

    @PostMapping
    public ResponseEntity<Response> addFavor(
            HttpServletRequest request,
            @RequestBody int bookId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        Object userAttr = request.getAttribute("user");
        if (userAttr == null || !(userAttr instanceof BookUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.noSession("user"));
        }
        BookUser user = (BookUser) userAttr;
        favorService.addFavor(user, bookId);
        return ResponseEntity.ok(ResponseHandler.success());
    }

    @DeleteMapping
    public ResponseEntity<Response> deleteFavor(
            HttpServletRequest request,
            @RequestBody int bookId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        Object userAttr = request.getAttribute("user");
        if (userAttr == null || !(userAttr instanceof BookUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.noSession("user"));
        }
        BookUser user = (BookUser) userAttr;
        favorService.deleteFavor(user, bookId);
        return ResponseEntity.ok(ResponseHandler.success());
    }
}
