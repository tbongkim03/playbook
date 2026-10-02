package playbook.encore.back.campus.service;

import playbook.encore.back.campus.dto.CampusRequestDto;
import playbook.encore.back.campus.dto.CampusResponseDto;

import java.util.List;

public interface CampusService {
    List<CampusResponseDto> getAllCampuses();
    
    List<CampusResponseDto> getActiveCampuses();
    
    CampusResponseDto getCampusById(Integer seqCampus);
    
    CampusResponseDto createCampus(CampusRequestDto campusRequestDto);
    
    CampusResponseDto updateCampus(Integer seqCampus, CampusRequestDto campusRequestDto);
    
    void deleteCampus(Integer seqCampus);
}

