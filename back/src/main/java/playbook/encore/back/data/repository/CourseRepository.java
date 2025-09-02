package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.data.entity.Course;

import java.time.LocalDate;
import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Integer> {

    @Query("SELECT c FROM Course c WHERE DATE(c.finishDtCourse) = DATE(:targetDate)")
    List<Course> findCoursesEndingInDays(@Param("targetDate") LocalDate targetDate);
}
