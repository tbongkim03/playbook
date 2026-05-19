package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.data.entity.Admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByIdAdmin(String idAdmin);

    Optional<Admin> findByDcAdmin(String discordUsername);

    @Query("""
        select a from Admin a
        left join fetch a.seqCampus
        where a.idAdmin = :userId
    """)
    Optional<Admin> findByIdAdminWithCampus(String userId);

    @Query("""
        select a from Admin a
        left join fetch a.seqCampus
    """)
    List<Admin> findAllWithCampus();

    @Query("""
        select a from Admin a
        left join fetch a.seqCampus
        where a.seqCampus.seqCampus = :campusId
    """)
    List<Admin> findAllWithCampusByCampusId(@Param("campusId") Integer campusId);

    // 대출가능, 대출불가, 연체중 상태 일괄 변경
    @Modifying
    @Query("""
    UPDATE Admin a
    SET a.statusAdmin = CASE
        WHEN EXISTS (SELECT h FROM History h
                        WHERE h.seqAdmin = a AND h.returnDt IS NULL 
                        AND h.bookDt < :overdueDate)
            THEN 'overdue'
        WHEN EXISTS (SELECT h FROM History h
                        WHERE h.seqAdmin = a AND h.returnDt IS NULL)
            THEN 'stop'
        ELSE 'available'
        END
""")
    void updateAllAdminStatus(@Param("overdueDate") LocalDate overdueDate);
}
