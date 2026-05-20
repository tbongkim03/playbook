package playbook.encore.back.sort.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.sort.entity.SortFirst;
import playbook.encore.back.sort.entity.SortSecond;

/** 도서 소분류(SortSecond) CRUD — 기본 JpaRepository 기능만 사용 */
public interface SortSecondRepository extends JpaRepository<SortSecond, Integer> {
}
