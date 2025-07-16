package playbook.encore.back.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import playbook.encore.back.data.dao.CourseDAO;
import playbook.encore.back.data.dto.course.CourseRequestDto;
import playbook.encore.back.data.dto.course.CourseResponseDto;
import playbook.encore.back.data.entity.Course;
import playbook.encore.back.data.repository.CourseRepository;
import playbook.encore.back.service.CourseService;

@Service
public class CourseServiceImpl implements CourseService{

    private final CourseDAO courseDAO;
    private final CourseRepository courseRepository;

    @Autowired
    public CourseServiceImpl(CourseDAO courseDAO, CourseRepository courseRepository) {
        this.courseDAO = courseDAO;
        this.courseRepository = courseRepository;
    }

    private CourseResponseDto convertToDto(Course entity) {
        return new CourseResponseDto(entity.getSeqCourse(), entity.getNameCourse(), entity.getStartDtCourse(), entity.getFinistDtCourse());
    }

    @Override
    public List<CourseResponseDto> getAllCourse() {
        List<Course> courses = courseDAO.selectAllCourse();
        List<CourseResponseDto> responseList = new ArrayList<>();

        for (Course course : courses) {
            CourseResponseDto courseResponseDto = convertToDto(course);
            responseList.add(courseResponseDto);
        }

        return responseList;
    }

    @Override
    @Transactional
    public CourseResponseDto insertCourse(CourseRequestDto courseRequestDto) {
        Course course = Course.builder()
            .nameCourse(courseRequestDto.getNameCourse())
            .startDtCourse(courseRequestDto.getStartDtCourse())
            .finistDtCourse(courseRequestDto.getFinishDtCourse())
            .build();
        Course savedCourse = courseDAO.insertCourse(course);
        CourseResponseDto courseResponseDto = convertToDto(savedCourse);
        return courseResponseDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseDto changeCourse(Integer courseId, CourseRequestDto courseRequestDto) {
        Course existingCourse = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("해당 과정은 존재하지 않습니다."));
        
        Course course = Course.builder()
            .seqCourse(existingCourse.getSeqCourse())
            .nameCourse(courseRequestDto.getNameCourse())
            .startDtCourse(courseRequestDto.getStartDtCourse())
            .finistDtCourse(courseRequestDto.getFinishDtCourse())
            .build();

        Course changedCourse = courseDAO.updateCourse(course);

        CourseResponseDto courseResponseDto = convertToDto(changedCourse);

        return courseResponseDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseById(Integer courseId) {
        Course selectedCourse = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("삭제에 실패하였습니다. 해당 과정은 존재하지 않습니다."));
        
        courseDAO.deleteCourse(selectedCourse);
    }
    
}
