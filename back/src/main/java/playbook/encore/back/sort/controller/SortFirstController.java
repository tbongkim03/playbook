package playbook.encore.back.sort.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.sort.dto.SortFirstRequestDto;
import playbook.encore.back.sort.dto.SortFirstResponseDto;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.sort.service.SortFirstService;

import java.util.List;

@RestController
@RequestMapping("/subjects")
public class SortFirstController {

    private final SortFirstService sortFirstService;

    @Autowired
    public SortFirstController(SortFirstService sortFirstService) {
        this.sortFirstService = sortFirstService;
    }

    @GetMapping
    public ResponseEntity<Response> getSortFirstAll() throws Exception {
        List<SortFirstResponseDto> sortFirstResponseDto = sortFirstService.getAllSortFirst();
        return ResponseEntity.ok(ResponseHandler.success(sortFirstResponseDto));
    }

    @PostMapping
    public ResponseEntity<Response> postSortFirst(
            HttpServletRequest request,
            @RequestBody @Valid SortFirstRequestDto sortFirstRequestDto
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            SortFirstResponseDto sortFirstResponseDto = sortFirstService.insertSortFirst(sortFirstRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseHandler.success(sortFirstResponseDto));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> putSortFirstById(
            HttpServletRequest request,
            @PathVariable("id") Integer sortFirstId,
            @RequestBody @Valid SortFirstRequestDto sortFirstRequestDto
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            SortFirstResponseDto sortFirstResponseDto = sortFirstService.changeSortFirst(sortFirstId, sortFirstRequestDto);
            return ResponseEntity.ok(ResponseHandler.success(sortFirstResponseDto));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteSortFirst(
            HttpServletRequest request,
            @PathVariable("id") Integer sortFirstId
    ) throws Exception {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            sortFirstService.deleteSortFirstById(sortFirstId);
            return ResponseEntity.ok(ResponseHandler.success());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
    }
}
