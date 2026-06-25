package playbook.encore.back.accesslog.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import playbook.encore.back.accesslog.entity.AccessLog;

import java.time.LocalDateTime;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    /** 전체 접속이력 (최신순 페이징) */
    Page<AccessLog> findAllByOrderByAccessedAtDesc(Pageable pageable);

    /** 접속자 유형 기준 필터 */
    Page<AccessLog> findByActorTypeOrderByAccessedAtDesc(String actorType, Pageable pageable);

    /** 결과(SUCCESS/FAIL) 기준 필터 */
    Page<AccessLog> findByResultOrderByAccessedAtDesc(String result, Pageable pageable);

    /** 접속자 유형 + 결과 복합 필터 */
    Page<AccessLog> findByActorTypeAndResultOrderByAccessedAtDesc(String actorType, String result, Pageable pageable);

    /** 1년 경과 이력 일괄 삭제 (약관 이행용) */
    @Modifying
    @Query("DELETE FROM AccessLog a WHERE a.accessedAt < :cutoff")
    int deleteOlderThan(LocalDateTime cutoff);
}
