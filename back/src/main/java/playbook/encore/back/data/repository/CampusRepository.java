package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.data.entity.Campus;

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
}
