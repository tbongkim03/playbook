package playbook.encore.back.sort.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.sort.entity.SortFirst;
import playbook.encore.back.sort.entity.SortSecond;

import java.util.List;

/** 도서 소분류(SortSecond) CRUD — 기본 JpaRepository 기능만 사용 */
public interface SortSecondRepository extends JpaRepository<SortSecond, Integer> {

    /** 중분류 한글명으로 조회 (엑셀 업로드 시 이름→엔티티 매핑) */
    List<SortSecond> findByKorSortSecond(String korSortSecond);
}
