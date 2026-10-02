package playbook.encore.back.course.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.course.entity.Course;

import java.time.LocalDate;
import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Integer> {

    /** 전체 과정 목록 조회 (캠퍼스 페치 조인) */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.seqCampus")
    List<Course> findAllWithCampus();

    /** 캠퍼스별 과정 목록 조회 (캠퍼스 페치 조인) */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.seqCampus WHERE c.seqCampus.seqCampus = :campusId")
    List<Course> findAllWithCampusByCampusId(@Param("campusId") Integer campusId);

    /** 특정 날짜에 종료되는 과정 조회 (스케줄러용 - 종료 알림) */
    @Query("SELECT c FROM Course c WHERE DATE(c.finishDtCourse) = DATE(:targetDate)")
    List<Course> findCoursesEndingInDays(@Param("targetDate") LocalDate targetDate);

    /** 특정 날짜 이전에 종료된 과정 조회 (스케줄러용 - 만료 과정 상태 변경) */
    @Query("SELECT c FROM Course c WHERE c.finishDtCourse < :currentDate")
    List<Course> findCoursesFinishedBefore(@Param("currentDate") LocalDate currentDate);
}
