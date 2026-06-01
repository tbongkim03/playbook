package playbook.encore.back.campus.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.campus.dto.CampusRequestDto;
import playbook.encore.back.campus.dto.CampusResponseDto;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.common.util.AuthUtil;
import playbook.encore.back.campus.service.CampusService;

@RestController
@RequestMapping("/campus")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    @GetMapping
    public ResponseEntity<Response> getAllActiveCampuses() {
        return ResponseEntity.ok(ResponseHandler.success(campusService.getActiveCampuses()));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllCampuses(HttpServletRequest request) {
        AuthUtil.requireAdmin(request);
        return ResponseEntity.ok(ResponseHandler.success(campusService.getAllCampuses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getCampusById(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus
    ) {
        AuthUtil.requireAdmin(request);
        try {
            CampusResponseDto campus = campusService.getCampusById(seqCampus);
            return ResponseEntity.ok(ResponseHandler.success(campus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseHandler.noData());
        }
    }

    @PostMapping
    public ResponseEntity<Response> createCampus(
            HttpServletRequest request,
            @RequestBody @Valid CampusRequestDto campusRequestDto
    ) {
        AuthUtil.requireAdmin(request);
        CampusResponseDto campus = campusService.createCampus(campusRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseHandler.success(campus));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateCampus(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus,
            @RequestBody @Valid CampusRequestDto campusRequestDto
    ) {
        AuthUtil.requireAdmin(request);
        CampusResponseDto campus = campusService.updateCampus(seqCampus, campusRequestDto);
        return ResponseEntity.ok(ResponseHandler.success(campus));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteCampus(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus
    ) {
        AuthUtil.requireAdmin(request);
        try {
            campusService.deleteCampus(seqCampus);
            return ResponseEntity.ok(ResponseHandler.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseHandler.noData());
        }
    }
}
