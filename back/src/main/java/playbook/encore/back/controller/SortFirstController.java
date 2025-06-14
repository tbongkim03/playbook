package playbook.encore.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dao.SortFirstDAO;
import playbook.encore.back.data.dto.sortFirst.SortFirstRequestDto;
import playbook.encore.back.data.dto.sortFirst.SortFirstResponseDto;
import playbook.encore.back.service.SortFirstService;

import java.util.List;

@RestController
@RequestMapping("/subjects")
public class SortFirstController {

    private final SortFirstService sortFirstService;
    private final SortFirstDAO sortFirstDAO;

    @Autowired
    public SortFirstController(SortFirstService sortFirstService, SortFirstDAO sortFirstDAO) {
        this.sortFirstService = sortFirstService;
        this.sortFirstDAO = sortFirstDAO;
    }

    @GetMapping
    public ResponseEntity<List<SortFirstResponseDto>> getSortFirstAll() throws Exception{
        List<SortFirstResponseDto> sortFirstResponseDto = sortFirstService.getAllSortFirst();
        return ResponseEntity.status(HttpStatus.OK).body(sortFirstResponseDto);
    }

    @PostMapping()
    public ResponseEntity<SortFirstResponseDto> postSortFirst(@RequestBody SortFirstRequestDto sortFirstRequestDto) throws Exception {
        SortFirstResponseDto sortFirstResponseDto = sortFirstService.insertSortFirst(sortFirstRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(sortFirstResponseDto);
    }

    @PutMapping
    public ResponseEntity<SortFirstResponseDto> putSortFirst(@RequestBody SortFirstRequestDto sortFirstRequestDto) throws Exception{
        SortFirstResponseDto sortFirstResponseDto = sortFirstService.changeSortFirst(sortFirstRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(sortFirstResponseDto);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteSortFirstById(@RequestBody SortFirstRequestDto sortFirstRequestDto) throws Exception {
        sortFirstService.deleteSortFirst(sortFirstRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body("해당 대분류를 삭제에 성공하였습니다.");
    }
}
