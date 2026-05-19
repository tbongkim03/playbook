package playbook.encore.back.course.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.course.entity.Course;

import java.time.LocalDate;
import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Integer> {

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.seqCampus")
    List<Course> findAllWithCampus();

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.seqCampus WHERE c.seqCampus.seqCampus = :campusId")
    List<Course> findAllWithCampusByCampusId(@Param("campusId") Integer campusId);

    @Query("SELECT c FROM Course c WHERE DATE(c.finishDtCourse) = DATE(:targetDate)")
    List<Course> findCoursesEndingInDays(@Param("targetDate") LocalDate targetDate);

    @Query("SELECT c FROM Course c WHERE c.finishDtCourse < :currentDate")
    List<Course> findCoursesFinishedBefore(@Param("currentDate") LocalDate currentDate);
}
