package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dao.SortFirstDAO;
import playbook.encore.back.data.dto.sortFirst.SortFirstRequestDto;
import playbook.encore.back.data.dto.sortFirst.SortFirstResponseDto;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.SortFirstService;

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
    public ResponseEntity<List<SortFirstResponseDto>> getSortFirstAll() throws Exception{
        List<SortFirstResponseDto> sortFirstResponseDto = sortFirstService.getAllSortFirst();
        return ResponseEntity.status(HttpStatus.OK).body(sortFirstResponseDto);
    }

    @PostMapping
    public ResponseEntity<?> postSortFirst(
            HttpServletRequest request,
            @RequestBody SortFirstRequestDto sortFirstRequestDto
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            SortFirstResponseDto sortFirstResponseDto = sortFirstService.insertSortFirst(sortFirstRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(sortFirstResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> putSortFirstById(
            HttpServletRequest request,
            @PathVariable("id") Integer sortFirstId,
            @RequestBody SortFirstRequestDto sortFirstRequestDto
    ) throws Exception{
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            SortFirstResponseDto sortFirstResponseDto = sortFirstService.changeSortFirst(sortFirstId, sortFirstRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(sortFirstResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSortFirst(
            HttpServletRequest request,
            @PathVariable("id") Integer sortFirstId
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            sortFirstService.deleteSortFirstById(sortFirstId);
            return ResponseEntity.status(HttpStatus.OK).body("삭제를 수행하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }
}
