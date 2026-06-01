package playbook.encore.back.favor.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.favor.dto.FavorResponseDto;
import playbook.encore.back.common.util.AuthUtil;
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
        BookUser user = AuthUtil.getUser(request);
        List<FavorResponseDto> favorData = favorService.getFavorList(user);
        return ResponseEntity.ok(ResponseHandler.success(favorData));
    }

    @PostMapping
    public ResponseEntity<Response> addFavor(
            HttpServletRequest request,
            @RequestBody int bookId
    ) throws Exception {
        BookUser user = AuthUtil.getUser(request);
        favorService.addFavor(user, bookId);
        return ResponseEntity.ok(ResponseHandler.success());
    }

    @DeleteMapping
    public ResponseEntity<Response> deleteFavor(
            HttpServletRequest request,
            @RequestBody int bookId
    ) throws Exception {
        BookUser user = AuthUtil.getUser(request);
        favorService.deleteFavor(user, bookId);
        return ResponseEntity.ok(ResponseHandler.success());
    }
}
