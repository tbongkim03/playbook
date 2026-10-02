package playbook.encore.back.sort.service;

import playbook.encore.back.sort.dto.SortSecondRequestDto;
import playbook.encore.back.sort.dto.SortSecondResponseDto;

import java.util.List;

public interface SortSecondService {
    SortSecondResponseDto insertSortSecond(SortSecondRequestDto sortSecondRequestDto);
    List<SortSecondResponseDto> getAllSortSecond() throws Exception;
    SortSecondResponseDto changeSortSecond(Integer sortSecondId, SortSecondRequestDto sortSecondRequestDto) throws Exception;
    void deleteSortSecondById(Integer sortSecondId) throws Exception;
}
