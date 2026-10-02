package playbook.encore.back.course.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.course.dto.CourseRequestDto;
import playbook.encore.back.course.dto.CourseResponseDto;
import playbook.encore.back.course.service.CourseService;

import playbook.encore.back.common.excel.ExcelUtil;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<Response> getCourseALll(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        Integer campusId = requestCampusId;
        if (campusId == null) {
            campusId = (Integer) request.getAttribute("campusId");
        }
        return ResponseEntity.ok(ResponseHandler.success(courseService.getAllCourse(campusId)));
    }

    @PostMapping
    public ResponseEntity<Response> postCourse(@RequestBody @Valid CourseRequestDto courseRequestDto) throws Exception {
        CourseResponseDto courseResponseDto = courseService.insertCourse(courseRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseHandler.success(courseResponseDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> putCourseById(
            @PathVariable("id") Integer courseId,
            @RequestBody @Valid CourseRequestDto courseRequestDto
    ) throws Exception {
        CourseResponseDto courseResponseDto = courseService.changeCourse(courseId, courseRequestDto);
        return ResponseEntity.ok(ResponseHandler.success(courseResponseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteCourse(@PathVariable("id") Integer courseId) throws Exception {
        courseService.deleteCourseById(courseId);
        return ResponseEntity.ok(ResponseHandler.success());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception {
        Integer campusId = requestCampusId != null ? requestCampusId : (Integer) request.getAttribute("campusId");
        byte[] data = courseService.exportExcel(campusId);
        return ExcelUtil.toResponse(data, "과정목록");
    }
}
