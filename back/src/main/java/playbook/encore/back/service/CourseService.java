package playbook.encore.back.service;

import java.util.List;

import playbook.encore.back.data.dto.course.CourseRequestDto;
import playbook.encore.back.data.dto.course.CourseResponseDto;

public interface CourseService {

    List<CourseResponseDto> getAllCourse();

    CourseResponseDto insertCourse(CourseRequestDto courseRequestDto);

    CourseResponseDto changeCourse(Integer courseId, CourseRequestDto courseRequestDto);

    void deleteCourseById(Integer courseId);
    
}
