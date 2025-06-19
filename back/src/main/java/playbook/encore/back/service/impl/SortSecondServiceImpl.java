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
    @Transactional(rollbackFor = Exception.class)
    public SortSecondResponseDto changeSortSecond(Integer sortSecondId, SortSecondRequestDto sortSecondRequestDto) throws Exception {
        SortFirst sortFirst = sortFirstRepository.findById(sortSecondRequestDto.getSeqSortFirst())
                .orElseThrow(() -> new IllegalArgumentException("해당 대분류가 존재하지 않습니다."));

        SortSecond changedSortSecond = sortSecondDAO.updateSortSecond(
                sortSecondId,
                sortFirst.getSeqSortFirst(),
                sortSecondRequestDto.getKorSortSecond(),
                sortSecondRequestDto.getNameSortSecond()
        );

        SortSecondResponseDto sortSecondResponseDto = convertToDto(changedSortSecond);
        return sortSecondResponseDto;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSortSecondById(Integer sortSecondId) throws Exception {
        SortSecond selectedSortSecond = sortSecondRepository.findById(sortSecondId)
                .orElseThrow(() -> new IllegalArgumentException("해당 소분류가 존재하지 않습니다."));

        sortSecondDAO.deleteSortSecond(selectedSortSecond.getSeqSortSecond());
    }
}
