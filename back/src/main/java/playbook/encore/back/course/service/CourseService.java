package playbook.encore.back.course.service;

import java.util.List;

import playbook.encore.back.course.dto.CourseRequestDto;
import playbook.encore.back.course.dto.CourseResponseDto;

public interface CourseService {

    List<CourseResponseDto> getAllCourse(Integer campusId);

    CourseResponseDto insertCourse(CourseRequestDto courseRequestDto);

    CourseResponseDto changeCourse(Integer courseId, CourseRequestDto courseRequestDto);

    void deleteCourseById(Integer courseId);
    
}
