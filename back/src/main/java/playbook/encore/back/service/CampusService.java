package playbook.encore.back.service;

import playbook.encore.back.data.dto.campus.CampusRequestDto;
import playbook.encore.back.data.dto.campus.CampusResponseDto;

import java.util.List;

public interface CampusService {
    List<CampusResponseDto> getAllCampuses();
    
    List<CampusResponseDto> getActiveCampuses();
    
    CampusResponseDto getCampusById(Integer seqCampus);
    
    CampusResponseDto createCampus(CampusRequestDto campusRequestDto);
    
    CampusResponseDto updateCampus(Integer seqCampus, CampusRequestDto campusRequestDto);
    
    void deleteCampus(Integer seqCampus);
}

