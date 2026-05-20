package playbook.encore.back.sort.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.sort.dao.SortFirstDAO;
import playbook.encore.back.sort.dto.SortFirstRequestDto;
import playbook.encore.back.sort.dto.SortFirstResponseDto;
import playbook.encore.back.sort.entity.SortFirst;
import playbook.encore.back.sort.dao.SortFirstRepository;
import playbook.encore.back.sort.service.SortFirstService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
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
        return new SortFirstResponseDto(entity.getSeqSortFirst(), entity.getKorSortFirst(), entity.getNameSortFirst());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SortFirstResponseDto insertSortFirst(SortFirstRequestDto sortFirstRequestDto) {
        log.info("[SortFirstService] 대분류 등록 - name: {}", sortFirstRequestDto.getNameSortFirst());
        SortFirst sortFirst = new SortFirst();
        sortFirst.setKorSortFirst(sortFirstRequestDto.getKorSortFirst());
        sortFirst.setNameSortFirst(sortFirstRequestDto.getNameSortFirst());

        SortFirst savedSortFirst = sortFirstDAO.insertSortFirst(sortFirst);

        SortFirstResponseDto sortFirstResponseDto = convertToDto(savedSortFirst);

        return sortFirstResponseDto;
    }

    @Override
    public List<SortFirstResponseDto> getAllSortFirst() throws Exception {
        log.info("[SortFirstService] 전체 대분류 조회");
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
    public SortFirstResponseDto changeSortFirst(Integer sortFirstId, SortFirstRequestDto sortFirstRequestDto) throws Exception {
        log.info("[SortFirstService] 대분류 수정 - sortFirstId: {}", sortFirstId);
        Optional<SortFirst> optionalSortFirst = sortFirstRepository.findById(sortFirstId);
        if (optionalSortFirst.isPresent()) {
            SortFirst changedSortFirst = sortFirstDAO.updateSortFirst(
                    sortFirstId,
                    sortFirstRequestDto.getKorSortFirst(),
                    sortFirstRequestDto.getNameSortFirst()
            );

            SortFirstResponseDto sortFirstResponseDto = convertToDto(changedSortFirst);
            return sortFirstResponseDto;
        } else {
            throw new Exception("수정에 실패하였습니다. 해당 대분류는 존재하지 않습니다.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSortFirstById(Integer sortFirstId) throws Exception {
        log.info("[SortFirstService] 대분류 삭제 - sortFirstId: {}", sortFirstId);
        Optional<SortFirst> optionalSortFirst = sortFirstRepository.findById(sortFirstId);
        if (optionalSortFirst.isPresent()) {
            sortFirstRepository.deleteById(optionalSortFirst.get().getSeqSortFirst());
        } else {
            throw new Exception("삭제에 실패하였습니다. 해당 대분류는 존재하지 않습니다.");
        }
    }
}
