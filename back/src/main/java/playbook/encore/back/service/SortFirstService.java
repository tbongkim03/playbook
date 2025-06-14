package playbook.encore.back.service;

import playbook.encore.back.data.dto.sortFirst.SortFirstRequestDto;
import playbook.encore.back.data.dto.sortFirst.SortFirstResponseDto;

import java.util.List;

public interface SortFirstService {
    SortFirstResponseDto insertSortFirst(SortFirstRequestDto sortFirstRequestDto);
    List<SortFirstResponseDto> getAllSortFirst() throws Exception;
    SortFirstResponseDto changeSortFirst(SortFirstRequestDto sortFirstRequestDto) throws Exception;
    void deleteSortFirst(SortFirstRequestDto sortFirstRequestDto) throws Exception;

}
