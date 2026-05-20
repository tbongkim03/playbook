package playbook.encore.back.campus.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.campus.dto.CampusRequestDto;
import playbook.encore.back.campus.dto.CampusResponseDto;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.interceptor.LoginCheckInterceptor;
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
        Object roleAttr = request.getAttribute("ROLE");
        if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
            return ResponseEntity.ok(ResponseHandler.success(campusService.getAllCampuses()));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getCampusById(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus
    ) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                CampusResponseDto campus = campusService.getCampusById(seqCampus);
                return ResponseEntity.ok(ResponseHandler.success(campus));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseHandler.noData());
        }
    }

    @PostMapping
    public ResponseEntity<Response> createCampus(
            HttpServletRequest request,
            @RequestBody CampusRequestDto campusRequestDto
    ) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                CampusResponseDto campus = campusService.createCampus(campusRequestDto);
                return ResponseEntity.status(HttpStatus.CREATED).body(ResponseHandler.success(campus));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateCampus(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus,
            @RequestBody CampusRequestDto campusRequestDto
    ) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                CampusResponseDto campus = campusService.updateCampus(seqCampus, campusRequestDto);
                return ResponseEntity.ok(ResponseHandler.success(campus));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteCampus(
            HttpServletRequest request,
            @PathVariable("id") Integer seqCampus
    ) {
        try {
            Object roleAttr = request.getAttribute("ROLE");
            if (LoginCheckInterceptor.RoleType.ADMIN.equals(roleAttr)) {
                campusService.deleteCampus(seqCampus);
                return ResponseEntity.ok(ResponseHandler.success());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseHandler.noData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }
}
