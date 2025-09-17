package playbook.encore.back.data.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.data.entity.BookUser;

public interface BookUserRepository extends JpaRepository<BookUser, Integer> {
    Optional<BookUser> findByIdUser(String idUser);

    Optional<BookUser> findByDcUser(String discordUsername);

    @Query("""
    SELECT bu.nameUser, bu.idUser, bu.statusUser, bu.createdAt,
           c.nameCourse, c.startDtCourse, c.finishDtCourse
    FROM BookUser bu
    INNER JOIN bu.seqCourse c
""")
    List<Object[]> findAllUsersWithCourseDetails();

    // 대출가능, 대출불가, 연체중 상태 일괄 변경
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
}
