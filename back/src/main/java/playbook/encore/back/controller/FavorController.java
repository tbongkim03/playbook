package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dto.favor.FavorResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.FavorService;

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
    public ResponseEntity<?> getFavor(
            HttpServletRequest request
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
            return ResponseEntity.status(403).body("일반 회원만 접근 가능합니다.");
        }
        try {
            Object userAttr = request.getAttribute("user");
            if (userAttr == null || !(userAttr instanceof BookUser)) {
                return ResponseEntity.status(403).body("사용자 정보가 없습니다.");
            }
            BookUser user = (BookUser) request.getAttribute("user");
            List<FavorResponseDto> favorData = favorService.getFavorList(user);
            return ResponseEntity.ok(favorData);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addFavor(
            HttpServletRequest request,
            @RequestBody int bookId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.USER.equals(roleAttr))
            return ResponseEntity.status(403).body("일반 회원만 접근 가능합니다.");
        try {
            Object userAttr = request.getAttribute("user");
            if (userAttr == null || !(userAttr instanceof BookUser)) {
                return ResponseEntity.status(403).body("사용자 정보가 없습니다.");
            }
            BookUser user = (BookUser) request.getAttribute("user");
            favorService.addFavor(user, bookId);
            return ResponseEntity.ok("즐겨찾기에 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteFavor(
            HttpServletRequest request,
            @RequestBody int bookId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (roleAttr == null || !LoginCheckInterceptor.RoleType.USER.equals(roleAttr)) {
            return ResponseEntity.status(403).body("일반 회원만 접근 가능합니다.");
        }
        try {
            Object userAttr = request.getAttribute("user");
            if (userAttr == null || !(userAttr instanceof BookUser)) {
                return ResponseEntity.status(403).body("사용자 정보가 없습니다.");
            }
            BookUser user = (BookUser) request.getAttribute("user");
            favorService.deleteFavor(user, bookId);
            return ResponseEntity.ok("즐겨찾기에서 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
