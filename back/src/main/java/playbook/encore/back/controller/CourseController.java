package playbook.encore.back.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.data.dao.CourseDAO;
import playbook.encore.back.data.dto.course.CourseRequestDto;
import playbook.encore.back.data.dto.course.CourseResponseDto;
import playbook.encore.back.service.CourseService;
@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;
    private final CourseDAO courseDAO;

    @Autowired
    public CourseController(CourseService courseService, CourseDAO courseDAO) {
        this.courseService = courseService;
        this.courseDAO = courseDAO;
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getCourseALll() throws Exception{
        List<CourseResponseDto> courseResponseDto = courseService.getAllCourse();
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
