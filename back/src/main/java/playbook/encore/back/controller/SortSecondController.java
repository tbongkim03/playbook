package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dao.SortSecondDAO;
import playbook.encore.back.data.dto.sortSecond.SortSecondRequestDto;
import playbook.encore.back.data.dto.sortSecond.SortSecondResponseDto;
import playbook.encore.back.service.SortSecondService;

import java.util.List;

@RestController
@RequestMapping("/subtitles")
public class SortSecondController {

    private final SortSecondService sortSecondService;
    private final SortSecondDAO sortSecondDAO;

    @Autowired
    public SortSecondController(SortSecondService sortSecondService, SortSecondDAO sortSecondDAO) {
        this.sortSecondService = sortSecondService;
        this.sortSecondDAO = sortSecondDAO;
    }

    @GetMapping
    public ResponseEntity<List<SortSecondResponseDto>> getSortSecondAll() throws Exception{
        List<SortSecondResponseDto> sortSecondResponseDto = sortSecondService.getAllSortSecond();
        return ResponseEntity.status(HttpStatus.OK).body(sortSecondResponseDto);
    }

    @PostMapping
    public ResponseEntity<SortSecondResponseDto> postSortSecond(@RequestBody SortSecondRequestDto sortSecondRequestDto) throws Exception {
        SortSecondResponseDto sortSecondResponseDto = sortSecondService.insertSortSecond(sortSecondRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(sortSecondResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SortSecondResponseDto> putSortSecondById(
            @PathVariable("id") Integer sortSecondId,
            @RequestBody SortSecondRequestDto sortSecondRequestDto
    ) throws Exception{
        sortSecondRequestDto.setSeqSortSecond(sortSecondId);
        SortSecondResponseDto sortSecondResponseDto = sortSecondService.changeSortSecond(sortSecondRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(sortSecondResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSortSecond(@PathVariable("id") Integer sortSecondId) throws Exception {
        sortSecondService.deleteSortSecondById(sortSecondId);
        return ResponseEntity.status(HttpStatus.OK).body("삭제를 수행하였습니다.");
    }

}
