package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.SortFirstDAO;
import playbook.encore.back.data.dto.sortFirst.SortFirstRequestDto;
import playbook.encore.back.data.dto.sortFirst.SortFirstResponseDto;
import playbook.encore.back.data.entity.SortFirst;
import playbook.encore.back.data.repository.SortFirstRepository;
import playbook.encore.back.service.SortFirstService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SortFirstServiceImpl implements SortFirstService {

    private final SortFirstDAO sortFirstDAO;
    private final SortFirstRepository sortFirstRepository;

    @Autowired
    public SortFirstServiceImpl(SortFirstDAO sortFirstDAO, SortFirstRepository sortFirstRepository) {
        this.sortFirstDAO = sortFirstDAO;
        this.sortFirstRepository = sortFirstRepository;
    }

    private SortFirstResponseDto convertToDto(SortFirst entity) {
        return new SortFirstResponseDto(entity.getKorSortFirst(), entity.getNameSortFirst());
    }

    @Override
    @Transactional
    public SortFirstResponseDto insertSortFirst(SortFirstRequestDto sortFirstRequestDto) {
        SortFirst sortFirst = new SortFirst();
        sortFirst.setKorSortFirst(sortFirstRequestDto.getKorSortFirst());
        sortFirst.setNameSortFirst(sortFirstRequestDto.getNameSortFirst());

        SortFirst savedSortFirst = sortFirstDAO.insertSortFirst(sortFirst);

        SortFirstResponseDto sortFirstResponseDto = convertToDto(savedSortFirst);

        return sortFirstResponseDto;
    }

    @Override
    public List<SortFirstResponseDto> getAllSortFirst() throws Exception {
        List<SortFirst> sortFirstList = sortFirstDAO.selectAllSortFirst();
        List<SortFirstResponseDto> responseList = new ArrayList<>();

        for (SortFirst sortFirst : sortFirstList) {
            SortFirstResponseDto sortFirstResponseDto = convertToDto(sortFirst);
            responseList.add(sortFirstResponseDto);
        }

        return responseList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public SortFirstResponseDto changeSortFirst(SortFirstRequestDto sortFirstRequestDto) throws Exception {
        Optional<SortFirst> optionalSortFirst = sortFirstRepository.findById(sortFirstRequestDto.getSeqSortFirst());
        if (optionalSortFirst.isPresent()) {
            SortFirst savedSortFirst = new SortFirst();
            savedSortFirst.setKorSortFirst(sortFirstRequestDto.getKorSortFirst());
            savedSortFirst.setNameSortFirst(sortFirstRequestDto.getNameSortFirst());
            SortFirst changedSortFirst = sortFirstDAO.updateSortFirst(savedSortFirst);

            SortFirstResponseDto sortFirstResponseDto = convertToDto(changedSortFirst);
            return sortFirstResponseDto;
        } else {
            throw new Exception("수정에 실패하였습니다. 해당 대분류는 존재하지 않습니다.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSortFirst(SortFirstRequestDto sortFirstRequestDto) throws Exception {
        Optional<SortFirst> optionalSortFirst = sortFirstRepository.findById(sortFirstRequestDto.getSeqSortFirst());
        if (optionalSortFirst.isPresent()) {
            sortFirstRepository.deleteById(optionalSortFirst.get().getSeqSortFirst());
        } else {
            throw new Exception("삭제에 실패하였습니다. 해당 대분류는 존재하지 않습니다.");
        }
    }
}
