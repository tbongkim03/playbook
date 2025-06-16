package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.data.entity.SortFirst;
import playbook.encore.back.data.entity.SortSecond;

public interface SortSecondRepository extends JpaRepository<SortSecond, Integer> {
}
