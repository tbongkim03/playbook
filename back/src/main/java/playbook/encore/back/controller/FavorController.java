package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
            return ResponseEntity.status(403).body("일반 유저만 접근 가능합니다.");
        }
        BookUser user = (BookUser) request.getAttribute("user");
        List<FavorResponseDto> favorData = favorService.getFavorList(user);

        return ResponseEntity.ok(favorData);
    }


}
