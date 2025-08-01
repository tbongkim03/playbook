package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.History;

import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Integer> {
    boolean existsBySeqBookAndSeqUserAndReturnDtIsNull(Book book, BookUser user);
    boolean existsBySeqBookAndSeqAdminAndReturnDtIsNull(Book book, Admin admin);

    Optional<History> findBySeqBookAndSeqAdminAndReturnDtIsNull(Book book, Admin admin);
    Optional<History> findBySeqBookAndSeqUserAndReturnDtIsNull(Book book, BookUser bookUser);
}
