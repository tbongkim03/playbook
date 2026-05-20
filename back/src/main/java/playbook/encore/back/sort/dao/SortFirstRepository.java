package playbook.encore.back.sort.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.sort.entity.SortFirst;

/** 도서 대분류(SortFirst) CRUD — 기본 JpaRepository 기능만 사용 */
public interface SortFirstRepository extends JpaRepository<SortFirst, Integer> {
}
