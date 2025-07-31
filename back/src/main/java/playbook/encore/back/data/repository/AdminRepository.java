package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.data.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
}
