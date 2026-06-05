package playbook.encore.back.auditlog.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.auditlog.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** 전체 감사로그 (최신순 페이징) */
    Page<AuditLog> findAllByOrderByAuditedAtDesc(Pageable pageable);

    /** 작업 유형 기준 필터 */
    Page<AuditLog> findByActionOrderByAuditedAtDesc(String action, Pageable pageable);

    /** 대상 유형 기준 필터 */
    Page<AuditLog> findByTargetTypeOrderByAuditedAtDesc(String targetType, Pageable pageable);

    /** 작업 유형 + 대상 유형 복합 필터 */
    Page<AuditLog> findByActionAndTargetTypeOrderByAuditedAtDesc(String action, String targetType, Pageable pageable);
}
