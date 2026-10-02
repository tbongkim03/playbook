package playbook.encore.back.sort.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.sort.dto.SortSecondRequestDto;
import playbook.encore.back.sort.dto.SortSecondResponseDto;
import playbook.encore.back.common.util.AuthUtil;
import playbook.encore.back.sort.service.SortSecondService;

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
    public ResponseEntity<Response> getSortSecondAll() throws Exception {
        List<SortSecondResponseDto> sortSecondResponseDto = sortSecondService.getAllSortSecond();
        return ResponseEntity.ok(ResponseHandler.success(sortSecondResponseDto));
    }

    @PostMapping
    public ResponseEntity<Response> postSortSecond(
            HttpServletRequest request,
            @RequestBody @Valid SortSecondRequestDto sortSecondRequestDto
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        SortSecondResponseDto sortSecondResponseDto = sortSecondService.insertSortSecond(sortSecondRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseHandler.success(sortSecondResponseDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> putSortSecondById(
            HttpServletRequest request,
            @PathVariable("id") Integer sortSecondId,
            @RequestBody @Valid SortSecondRequestDto sortSecondRequestDto
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        SortSecondResponseDto sortSecondResponseDto = sortSecondService.changeSortSecond(sortSecondId, sortSecondRequestDto);
        return ResponseEntity.ok(ResponseHandler.success(sortSecondResponseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteSortSecond(
            HttpServletRequest request,
            @PathVariable("id") Integer sortSecondId
    ) throws Exception {
        AuthUtil.requireAdmin(request);
        sortSecondService.deleteSortSecondById(sortSecondId);
        return ResponseEntity.ok(ResponseHandler.success());
    }
}
