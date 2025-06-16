package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.SortSecondDAO;
import playbook.encore.back.data.dto.sortSecond.SortSecondRequestDto;
import playbook.encore.back.data.dto.sortSecond.SortSecondResponseDto;
import playbook.encore.back.data.entity.SortFirst;
import playbook.encore.back.data.entity.SortSecond;
import playbook.encore.back.data.repository.SortFirstRepository;
import playbook.encore.back.data.repository.SortSecondRepository;
import playbook.encore.back.service.SortSecondService;

import java.util.List;

@Service
public class SortSecondServiceImpl implements SortSecondService {

    private final SortSecondDAO sortSecondDAO;
    private final SortSecondRepository sortSecondRepository;
    private final SortFirstRepository sortFirstRepository;

    @Autowired
    public SortSecondServiceImpl(SortSecondDAO sortSecondDAO, SortSecondRepository sortSecondRepository, SortFirstRepository sortFirstRepository) {
        this.sortSecondDAO = sortSecondDAO;
        this.sortSecondRepository = sortSecondRepository;
        this.sortFirstRepository = sortFirstRepository;
    }

    public SortSecondResponseDto convertToDto(SortSecond entity) {
        return new SortSecondResponseDto(
                entity.getSeqSortSecond(),
                entity.getSeqSortFirst().getSeqSortFirst(),
                entity.getKorSortSecond(),
                entity.getNameSortSecond());
    }

    @Override
    @Transactional
    public SortSecondResponseDto insertSortSecond(SortSecondRequestDto sortSecondRequestDto) {

        SortFirst sortFirst = sortFirstRepository.findById(sortSecondRequestDto.getSeqSortFirst())
                .orElseThrow(() -> new IllegalArgumentException("해당 대분류가 존재하지 않습니다"));


        SortSecond sortSecond = new SortSecond();
        sortSecond.setSeqSortFirst(sortFirst);
        sortSecond.setKorSortSecond(sortSecondRequestDto.getKorSortSecond());
        sortSecond.setNameSortSecond(sortSecondRequestDto.getNameSortSecond());

        SortSecond savedSortSecond = sortSecondDAO.insertSortSecond(sortSecond);

        SortSecondResponseDto sortSecondResponseDto = convertToDto(savedSortSecond);
        return sortSecondResponseDto;
    }

    @Override
    public List<SortSecondResponseDto> getAllSortSecond() throws Exception {
        List<SortSecond> sortSecondList = sortSecondDAO.selectAllSortSecond();

        List<SortSecondResponseDto> responseList = new java.util.ArrayList<>();
        for (SortSecond sortSecond : sortSecondList) {
            SortSecondResponseDto sortSecondResponseDto = convertToDto(sortSecond);
            responseList.add(sortSecondResponseDto);
        }

        return responseList;
    }

    @Override
    public SortSecondResponseDto changeSortSecond(SortSecondRequestDto sortSecondRequestDto) throws Exception {
        // TODO
        SortFirst sortFirst = sortFirstRepository.findById(sortSecondRequestDto.getSeqSortFirst())
                .orElseThrow(() -> new IllegalArgumentException("해당 대분류가 존재하지 않습니다."));

        SortSecond sortSecond = new SortSecond();
        sortSecond.setSeqSortFirst(sortFirst);
        sortSecond.setKorSortSecond(sortSecondRequestDto.getKorSortSecond());
        sortSecond.setNameSortSecond(sortSecondRequestDto.getNameSortSecond());

        SortSecond changedSortSecond = sortSecondDAO.updateSortSecond(sortSecond);

        SortSecondResponseDto sortSecondResponseDto = convertToDto(changedSortSecond);
        return sortSecondResponseDto;

    }

    @Override
    public void deleteSortSecond(SortSecondRequestDto sortSecondRequestDto) throws Exception {
        SortSecond selectedSortSecond = sortSecondRepository.findById(sortSecondRequestDto.getSeqSortSecond())
                .orElseThrow(() -> new IllegalArgumentException("삭제에 실패했습니다. 해당 소분류가 존재하지 않습니다: " + sortSecondRequestDto.getSeqSortSecond()));

        sortSecondRepository.delete(selectedSortSecond);
    }
}
