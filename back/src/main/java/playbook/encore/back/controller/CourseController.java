package playbook.encore.back.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dto.course.CourseRequestDto;
import playbook.encore.back.data.dto.course.CourseResponseDto;
import playbook.encore.back.service.CourseService;
@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getCourseALll(
            HttpServletRequest request,
            @RequestParam(value = "campusId", required = false) Integer requestCampusId
    ) throws Exception{
        // 쿼리 파라미터로 전달된 campusId가 있으면 우선 사용
        Integer campusId = requestCampusId;
        
        // 쿼리 파라미터가 없으면 interceptor에서 설정한 campusId 사용
        if (campusId == null) {
            campusId = (Integer) request.getAttribute("campusId");
        }
        
        List<CourseResponseDto> courseResponseDto = courseService.getAllCourse(campusId);
        return ResponseEntity.status(HttpStatus.OK).body(courseResponseDto);
    }

    @PostMapping
    public ResponseEntity<CourseResponseDto> postCourse(@RequestBody CourseRequestDto courseRequestDto) throws Exception {
        CourseResponseDto courseResponseDto = courseService.insertCourse(courseRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(courseResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDto> putCourseById(
            @PathVariable("id") Integer courseId,
            @RequestBody CourseRequestDto courseRequestDto
    ) throws Exception{
        CourseResponseDto courseResponseDto = courseService.changeCourse(courseId, courseRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(courseResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable("id") Integer courseId) throws Exception {
        courseService.deleteCourseById(courseId);
        return ResponseEntity.status(HttpStatus.OK).body("삭제를 수행하였습니다.");
    }
}
