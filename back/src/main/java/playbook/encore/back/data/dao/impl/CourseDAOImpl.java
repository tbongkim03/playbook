package playbook.encore.back.data.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import playbook.encore.back.data.dao.CourseDAO;
import playbook.encore.back.data.entity.Course;
import playbook.encore.back.data.repository.CourseRepository;

@Component
public class CourseDAOImpl implements CourseDAO{

    private final CourseRepository courseRepository;

    @Autowired
    public CourseDAOImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Course> selectAllCourse() {
        List<Course> courseList = courseRepository.findAll();

        return courseList;
    }

    @Override
    public Course insertCourse(Course course) {
        Course savedCourse = courseRepository.save(course);
        return savedCourse;
    }

    @Override
    public Course updateCourse(Course course) {
        Course selectedCourse = courseRepository.findById(course.getSeqCourse())
        .orElseThrow(() -> new IllegalArgumentException("해당 과정은 존재하지 않습니다."));

        Course updatedCourse;
        selectedCourse.setSeqCourse(course.getSeqCourse());
        selectedCourse.setNameCourse(course.getNameCourse());
        selectedCourse.setStartDtCourse(course.getStartDtCourse());
        selectedCourse.setFinistDtCourse(course.getFinistDtCourse());

        updatedCourse = courseRepository.save(selectedCourse);
        return updatedCourse;
    }

    @Override
    public void deleteCourse(Course course) {
        Optional<Course> optionalCourse = courseRepository.findById(course.getSeqCourse());

        if (optionalCourse.isPresent()) {
            Course selectedCourse = optionalCourse.get();

            courseRepository.delete(selectedCourse);
        } else {
            throw new IllegalArgumentException("해당 과정을 삭제하지 못하였습니다.");
        }
    }

    
    
}
