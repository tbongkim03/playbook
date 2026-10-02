package playbook.encore.back.course.dao;

import java.util.List;

import playbook.encore.back.course.entity.Course;

public interface CourseDAO {

    List<Course> selectAllCourse();

    Course insertCourse(Course course);

    Course updateCourse(Course course);

    void deleteCourse(Course course);
    
}
