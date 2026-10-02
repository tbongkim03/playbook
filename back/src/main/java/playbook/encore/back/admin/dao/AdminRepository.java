package playbook.encore.back.admin.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.admin.entity.Admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {

    /** 관리자 ID로 단건 조회 */
    Optional<Admin> findByIdAdmin(String idAdmin);

    /** 디스코드 사용자명으로 단건 조회 */
    Optional<Admin> findByDcAdmin(String discordUsername);

    /** 관리자 ID로 조회 (캠퍼스 페치 조인) */
    @Query("""
        select a from Admin a
        left join fetch a.seqCampus
        where a.idAdmin = :userId
    """)
    Optional<Admin> findByIdAdminWithCampus(String userId);

    /** 전체 관리자 목록 조회 (캠퍼스 페치 조인) */
    @Query("""
        select a from Admin a
        left join fetch a.seqCampus
    """)
    List<Admin> findAllWithCampus();

    /** 캠퍼스별 관리자 목록 조회 (캠퍼스 페치 조인) */
    @Query("""
        select a from Admin a
        left join fetch a.seqCampus
        where a.seqCampus.seqCampus = :campusId
    """)
    List<Admin> findAllWithCampusByCampusId(@Param("campusId") Integer campusId);

    /** 전체 관리자 대출 상태 일괄 갱신 (available / stop / overdue) */
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
