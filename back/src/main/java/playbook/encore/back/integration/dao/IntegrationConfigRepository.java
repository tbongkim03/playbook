package playbook.encore.back.integration.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.integration.entity.IntegrationConfig;

import java.util.List;
import java.util.Optional;

public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, Integer> {

    Optional<IntegrationConfig> findByConfigKey(String configKey);

    List<IntegrationConfig> findByCategory(String category);

    boolean existsByConfigKey(String configKey);
}
