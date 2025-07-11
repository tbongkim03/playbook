package playbook.encore.back.service;

import playbook.encore.back.data.dto.sortSecond.SortSecondRequestDto;
import playbook.encore.back.data.dto.sortSecond.SortSecondResponseDto;

import java.util.List;

public interface SortSecondService {
    SortSecondResponseDto insertSortSecond(SortSecondRequestDto sortSecondRequestDto);
    List<SortSecondResponseDto> getAllSortSecond() throws Exception;
    SortSecondResponseDto changeSortSecond(Integer sortSecondId, SortSecondRequestDto sortSecondRequestDto) throws Exception;
    void deleteSortSecondById(Integer sortSecondId) throws Exception;
}
