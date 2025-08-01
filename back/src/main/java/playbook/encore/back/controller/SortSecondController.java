package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dao.SortSecondDAO;
import playbook.encore.back.data.dto.sortSecond.SortSecondRequestDto;
import playbook.encore.back.data.dto.sortSecond.SortSecondResponseDto;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.SortSecondService;

import java.util.List;

@RestController
@RequestMapping("/subtitles")
public class SortSecondController {

    private final SortSecondService sortSecondService;

    @Autowired
    public SortSecondController(SortSecondService sortSecondService) {
        this.sortSecondService = sortSecondService;
    }

    @GetMapping
    public ResponseEntity<List<SortSecondResponseDto>> getSortSecondAll() throws Exception{
        List<SortSecondResponseDto> sortSecondResponseDto = sortSecondService.getAllSortSecond();
        return ResponseEntity.status(HttpStatus.OK).body(sortSecondResponseDto);
    }

    @PostMapping
    public ResponseEntity<?> postSortSecond(
            HttpServletRequest request,
            @RequestBody SortSecondRequestDto sortSecondRequestDto
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            SortSecondResponseDto sortSecondResponseDto = sortSecondService.insertSortSecond(sortSecondRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(sortSecondResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> putSortSecondById(
            HttpServletRequest request,
            @PathVariable("id") Integer sortSecondId,
            @RequestBody SortSecondRequestDto sortSecondRequestDto
    ) throws Exception{
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            SortSecondResponseDto sortSecondResponseDto = sortSecondService.changeSortSecond(sortSecondId, sortSecondRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(sortSecondResponseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSortSecond(
            HttpServletRequest request,
            @PathVariable("id") Integer sortSecondId
    ) throws Exception {
        if (request.getAttribute("ROLE") == LoginCheckInterceptor.RoleType.ADMIN) {
            sortSecondService.deleteSortSecondById(sortSecondId);
            return ResponseEntity.status(HttpStatus.OK).body("삭제를 수행하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");

    }

}
