package playbook.encore.back.sort.service;

import playbook.encore.back.sort.dto.SortFirstRequestDto;
import playbook.encore.back.sort.dto.SortFirstResponseDto;

import java.util.List;

public interface SortFirstService {
    SortFirstResponseDto insertSortFirst(SortFirstRequestDto sortFirstRequestDto);
    List<SortFirstResponseDto> getAllSortFirst() throws Exception;
    SortFirstResponseDto changeSortFirst(Integer sortFirstId, SortFirstRequestDto sortFirstRequestDto) throws Exception;
    void deleteSortFirstById(Integer sortFirstId) throws Exception;

}
