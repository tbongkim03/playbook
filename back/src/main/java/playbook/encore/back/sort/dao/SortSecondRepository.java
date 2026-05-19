package playbook.encore.back.sort.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.sort.entity.SortFirst;
import playbook.encore.back.sort.entity.SortSecond;

public interface SortSecondRepository extends JpaRepository<SortSecond, Integer> {
}
