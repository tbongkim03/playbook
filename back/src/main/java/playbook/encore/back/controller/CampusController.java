package playbook.encore.back.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dto.campus.CampusRequestDto;
import playbook.encore.back.data.dto.campus.CampusResponseDto;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
import playbook.encore.back.service.CampusService;

import java.util.List;

@RestController
@RequestMapping("/campus")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    /**
     * 활성화된 캠퍼스 목록 조회
     * Frontend에서 캠퍼스 드롭다운을 위해 사용
     */
    @GetMapping
    public ResponseEntity<List<CampusResponseDto>> getAllActiveCampuses() {
        List<CampusResponseDto> campuses = campusService.getActiveCampuses();
        return ResponseEntity.status(HttpStatus.OK).body(campuses);
    }

    /**
     * 모든 캠퍼스 목록 조회 (관리자용)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllCampuses(HttpServletRequest request) {
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            List<CampusResponseDto> campuses = campusService.getAllCampuses();
            return ResponseEntity.status(HttpStatus.OK).body(campuses);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    /**
     * 캠퍼스 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCampusById(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus
    ) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                CampusResponseDto campus = campusService.getCampusById(seqCampus);
                return ResponseEntity.status(HttpStatus.OK).body(campus);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * 캠퍼스 생성
     */
    @PostMapping
    public ResponseEntity<?> createCampus(
            HttpServletRequest request,
            @RequestBody CampusRequestDto campusRequestDto
    ) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                CampusResponseDto campus = campusService.createCampus(campusRequestDto);
                return ResponseEntity.status(HttpStatus.CREATED).body(campus);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 캠퍼스 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampus(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus,
            @RequestBody CampusRequestDto campusRequestDto
    ) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                CampusResponseDto campus = campusService.updateCampus(seqCampus, campusRequestDto);
                return ResponseEntity.status(HttpStatus.OK).body(campus);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 캠퍼스 삭제 (비활성화)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCampus(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus
    ) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                campusService.deleteCampus(seqCampus);
                return ResponseEntity.status(HttpStatus.OK).body("캠퍼스가 비활성화되었습니다.");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
