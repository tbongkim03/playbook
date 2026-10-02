package playbook.encore.back.integration.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import playbook.encore.back.integration.entity.CampusChannel;

import java.util.List;
import java.util.Optional;

public interface CampusChannelRepository extends JpaRepository<CampusChannel, Integer> {

    Optional<CampusChannel> findBySeqCampus_SeqCampus(Integer seqCampus);

    @Query("select cc from CampusChannel cc join fetch cc.seqCampus")
    List<CampusChannel> findAllWithCampus();
}
