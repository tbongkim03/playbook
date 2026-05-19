package playbook.encore.back.campus.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.campus.dto.CampusRequestDto;
import playbook.encore.back.campus.dto.CampusResponseDto;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.campus.dao.CampusRepository;
import playbook.encore.back.campus.service.CampusService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CampusServiceImpl implements CampusService {

    private final CampusRepository campusRepository;

    @Autowired
    public CampusServiceImpl(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
    }

    private CampusResponseDto convertToDto(Campus entity) {
        return new CampusResponseDto(
            entity.getSeqCampus(),
            entity.getNameCampus(),
            entity.getLocationCampus(),
            entity.isActive()
        );
    }

    @Override
    public List<CampusResponseDto> getAllCampuses() {
        log.info("[CampusService] 전체 캠퍼스 조회");
        List<Campus> campuses = campusRepository.findAll();
        return campuses.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<CampusResponseDto> getActiveCampuses() {
        log.info("[CampusService] 활성 캠퍼스 조회");
        List<Campus> campuses = campusRepository.findByIsActiveTrue();
        return campuses.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public CampusResponseDto getCampusById(Integer seqCampus) {
        log.info("[CampusService] 캠퍼스 단건 조회 - seqCampus: {}", seqCampus);
        Campus campus = campusRepository.findById(seqCampus)
            .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스는 존재하지 않습니다."));
        return convertToDto(campus);
    }

    @Override
    @Transactional
    public CampusResponseDto createCampus(CampusRequestDto campusRequestDto) {
        log.info("[CampusService] 캠퍼스 등록 - name: {}", campusRequestDto.getNameCampus());
        // 이름 중복 체크
        if (campusRepository.findByNameCampus(campusRequestDto.getNameCampus()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 캠퍼스 이름입니다.");
        }

        Campus campus = Campus.builder()
            .nameCampus(campusRequestDto.getNameCampus())
            .locationCampus(campusRequestDto.getLocationCampus())
            .isActive(campusRequestDto.getIsActive() != null ? campusRequestDto.getIsActive() : true)
            .build();

        Campus savedCampus = campusRepository.save(campus);
        return convertToDto(savedCampus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CampusResponseDto updateCampus(Integer seqCampus, CampusRequestDto campusRequestDto) {
        log.info("[CampusService] 캠퍼스 수정 - seqCampus: {}", seqCampus);
        Campus existingCampus = campusRepository.findById(seqCampus)
            .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스는 존재하지 않습니다."));

        // 이름 변경 시 중복 체크
        if (!existingCampus.getNameCampus().equals(campusRequestDto.getNameCampus())) {
            if (campusRepository.findByNameCampus(campusRequestDto.getNameCampus()).isPresent()) {
                throw new IllegalArgumentException("이미 존재하는 캠퍼스 이름입니다.");
            }
        }

        existingCampus.setNameCampus(campusRequestDto.getNameCampus());
        existingCampus.setLocationCampus(campusRequestDto.getLocationCampus());
        if (campusRequestDto.getIsActive() != null) {
            existingCampus.setActive(campusRequestDto.getIsActive());
        }

        Campus updatedCampus = campusRepository.save(existingCampus);
        return convertToDto(updatedCampus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCampus(Integer seqCampus) {
        log.info("[CampusService] 캠퍼스 삭제 - seqCampus: {}", seqCampus);
        Campus campus = campusRepository.findById(seqCampus)
            .orElseThrow(() -> new IllegalArgumentException("삭제에 실패하였습니다. 해당 캠퍼스는 존재하지 않습니다."));
        
        // 실제 삭제 대신 비활성화 처리 (데이터 무결성 유지)
        campus.setActive(false);
        campusRepository.save(campus);
    }
}

