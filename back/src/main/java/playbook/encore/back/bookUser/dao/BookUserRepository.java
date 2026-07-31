package playbook.encore.back.bookUser.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.course.entity.Course;

public interface BookUserRepository extends JpaRepository<BookUser, Integer> {

    /** 사용자 ID로 단건 조회 */
    Optional<BookUser> findByIdUser(String idUser);

    /** 디스코드 사용자명으로 단건 조회 */
    Optional<BookUser> findByDcUser(String discordUsername);

    /** 사용자 seq 로 소속 캠퍼스 ID 조회 (과정·캠퍼스 미연결 시 결과 없음 → 역할 미부여) */
    @Query("select c.seqCampus.seqCampus from BookUser bu join bu.seqCourse c where bu.seqUser = :seqUser")
    Optional<Integer> findCampusIdBySeqUser(@Param("seqUser") Integer seqUser);

    /** 전체 사용자 목록 조회 (과정 정보 포함, 전체 캠퍼스) */
    @Query("""
    SELECT bu.nameUser, bu.idUser, bu.statusUser, bu.createdAt,
           c.nameCourse, c.startDtCourse, c.finishDtCourse
    FROM BookUser bu
    INNER JOIN bu.seqCourse c
""")
    List<Object[]> findAllUsersWithCourseDetails();

    /** 사용자 ID로 조회 (과정·캠퍼스 페치 조인) */
    @Query("""
    select u from BookUser u
    left join fetch u.seqCourse c
    left join fetch c.seqCampus
    where u.idUser = :idUser
""")
    Optional<BookUser> findByIdUserWithCourseAndCampus(String idUser);

    /** 전체 사용자 대출 상태 일괄 갱신 (available / stop / overdue) */
    @Modifying
    @Query("""
    UPDATE BookUser bu
    SET bu.statusUser = CASE
        WHEN EXISTS (SELECT h FROM History h
                     WHERE h.seqUser = bu AND h.returnDt IS NULL
                     AND h.bookDt < :overdueDate)
            THEN 'overdue'
        WHEN EXISTS (SELECT h FROM History h
                     WHERE h.seqUser = bu AND h.returnDt IS NULL)
            THEN 'stop'
        ELSE 'available'
        END
""")
    void updateAllStatusUser(@Param("overdueDate") LocalDate overdueDate);

    /** 특정 과정 수강 사용자 목록 조회 */
    List<BookUser> findBySeqCourse(Course course);

    /** 특정 과정 사용자 상태를 stop으로 일괄 변경 (연체 중인 사용자 제외) */
    @Modifying
    @Query("""
        UPDATE BookUser bu
        SET bu.statusUser = 'stop'
        WHERE bu.seqCourse = :course
          AND NOT EXISTS (
              SELECT h FROM History h
              WHERE h.seqUser = bu
                AND h.returnDt IS NULL
                AND h.bookDt < :overdueDate
          )
    """)
    void updateStatusByCourse(@Param("course") Course course, @Param("overdueDate") LocalDate overdueDate);

    /** 존재하지 않는 과정을 참조하는 사용자 상태를 stop으로 일괄 변경 (연체 중인 사용자 제외) */
    @Modifying
    @Query("""
        UPDATE BookUser bu
        SET bu.statusUser = 'stop'
        WHERE NOT EXISTS (
              SELECT c FROM Course c WHERE c = bu.seqCourse
          )
          AND NOT EXISTS (
              SELECT h FROM History h
              WHERE h.seqUser = bu
                AND h.returnDt IS NULL
                AND h.bookDt < :overdueDate
          )
    """)
    void updateStatusForNonExistentCourses(@Param("overdueDate") LocalDate overdueDate);

    // ========== 캠퍼스 필터링 메서드 (추가) ==========

    /**
     * 캠퍼스별 사용자 목록 조회 (Course를 통해 캠퍼스 필터링)
     */
    @Query("""
    SELECT bu.nameUser, bu.idUser, bu.statusUser, bu.createdAt,
           c.nameCourse, c.startDtCourse, c.finishDtCourse
    FROM BookUser bu
    INNER JOIN bu.seqCourse c
    WHERE c.seqCampus.seqCampus = :campusId
""")
    List<Object[]> findAllUsersWithCourseDetailsByCampus(@Param("campusId") Integer campusId);
}
