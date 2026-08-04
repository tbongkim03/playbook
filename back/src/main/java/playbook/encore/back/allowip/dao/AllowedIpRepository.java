package playbook.encore.back.allowip.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.allowip.entity.AllowedIp;

import java.util.List;
import java.util.Optional;

public interface AllowedIpRepository extends JpaRepository<AllowedIp, Integer> {

    /** 활성 규칙 전체 (필터 캐시·자기차단 시뮬레이션용) */
    @Query("SELECT a FROM AllowedIp a WHERE a.isActive = true ORDER BY a.seqAllowedIp ASC")
    List<AllowedIp> findActiveRules();

    /** 캠퍼스까지 페치 조인한 전체 목록 (open-in-view=false 대응) */
    @Query("SELECT a FROM AllowedIp a LEFT JOIN FETCH a.seqCampus ORDER BY a.seqAllowedIp ASC")
    List<AllowedIp> findAllWithCampus();

    /** 단건 + 캠퍼스 페치 조인 */
    @Query("SELECT a FROM AllowedIp a LEFT JOIN FETCH a.seqCampus WHERE a.seqAllowedIp = :seqAllowedIp")
    Optional<AllowedIp> findByIdWithCampus(@Param("seqAllowedIp") Integer seqAllowedIp);

    /** 동일 IP 표기 규칙 (같은 스코프 중복 검사용) */
    List<AllowedIp> findByIpValue(String ipValue);

    /**
     * 소프트 삭제된 전역 규칙 조회 (되살리기용).
     * uk_allowed_ip_value_campus(ip_value, seq_campus) 때문에 삭제된 규칙과 같은 값을 다시 INSERT 하면
     * 유니크 제약에 걸린다. 네이티브 쿼리는 @Where(use_yn='Y') 를 타지 않으므로 삭제분까지 볼 수 있다.
     */
    @Query(value = "SELECT * FROM tb_allowed_ip WHERE ip_value = :ipValue AND use_yn = 'N' AND seq_campus IS NULL",
            nativeQuery = true)
    List<AllowedIp> findSoftDeletedGlobal(@Param("ipValue") String ipValue);

    /** 소프트 삭제된 캠퍼스 규칙 조회 (되살리기용) */
    @Query(value = "SELECT * FROM tb_allowed_ip WHERE ip_value = :ipValue AND use_yn = 'N' AND seq_campus = :seqCampus",
            nativeQuery = true)
    List<AllowedIp> findSoftDeletedByCampus(@Param("ipValue") String ipValue, @Param("seqCampus") Integer seqCampus);

    /**
     * 소프트 삭제분까지 포함한 전체 행 수.
     * 부트스트랩 시딩은 "테이블이 진짜로 비어 있을 때"만 수행해야 하므로
     * @Where(use_yn='Y') 가 적용되는 count() 대신 이 네이티브 카운트를 쓴다.
     * (사용자가 규칙을 전부 삭제한 뒤 재기동해도 시드가 부활하지 않는다)
     */
    @Query(value = "SELECT COUNT(*) FROM tb_allowed_ip", nativeQuery = true)
    long countAllIncludingDeleted();
}
