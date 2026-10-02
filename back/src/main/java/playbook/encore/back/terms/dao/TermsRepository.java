package playbook.encore.back.terms.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.terms.entity.Terms;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Integer> {
    Optional<Terms> findByTermsType(String termsType);
}
