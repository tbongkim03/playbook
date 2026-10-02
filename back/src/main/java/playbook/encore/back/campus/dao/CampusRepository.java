package playbook.encore.back.campus.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import playbook.encore.back.campus.entity.Campus;

import java.util.List;
import java.util.Optional;

/**
 * CampusRepository
 * 캠퍼스 데이터 접근을 위한 Repository
 */
public interface CampusRepository extends JpaRepository<Campus, Integer> {
    /**
     * 활성 상태인 캠퍼스 목록 조회
     * @return 활성 캠퍼스 리스트
     */
    List<Campus> findByIsActiveTrue();
    
    /**
     * 캠퍼스 이름으로 조회
     * @param nameCampus 캠퍼스 이름
     * @return 캠퍼스 Optional
     */
    Optional<Campus> findByNameCampus(String nameCampus);

    /**
     * Soft Delete 된 행까지 포함한 전체 캠퍼스 수.
     *
     * <p>엔티티에 {@code @Where(use_yn = 'Y')} 가 걸려 있어 {@code count()} 는 삭제된 행을 세지 않는다.
     * 신규 설치 판정에는 그 구분이 없어야 한다 — 운영 중 캠퍼스를 전부 삭제한 DB를
     * "빈 DB" 로 오인해 시더가 새 캠퍼스를 만들어내면 안 되기 때문이다.
     */
    @Query(value = "SELECT COUNT(*) FROM tb_campus", nativeQuery = true)
    long countAllIncludingDeleted();
}
