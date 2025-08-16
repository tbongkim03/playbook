package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.History;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Integer> {
    // 동일인이 같은 책을 빌린 것인지 확인
    boolean existsBySeqBookAndSeqUserAndReturnDtIsNull(Book book, BookUser user);
    boolean existsBySeqBookAndSeqAdminAndReturnDtIsNull(Book book, Admin admin);

    // 반납할 대여 기록이 있는지
    Optional<History> findBySeqBookAndSeqAdminAndReturnDtIsNull(Book book, Admin admin);
    Optional<History> findBySeqBookAndSeqUserAndReturnDtIsNull(Book book, BookUser bookUser);

    // 빌린 책인지 확인
    boolean existsBySeqBookAndReturnDtIsNull(Book book);
    // 대여 중인 책 수 확인
    int countBySeqUserAndReturnDtIsNull(BookUser bookUser);

    int countByBookDtIsNotNull();
    int countByBookDtIsNotNullAndReturnDtIsNotNull();
    int countByBookDtIsNotNullAndReturnDtIsNull();
    int countByReturnDtIsNullAndBookDtBefore(LocalDate localDate);

    // 연체 여부 확인
    List<History> findAllBySeqUserAndReturnDtIsNull(BookUser bookUser);

    // 개인 대여 기록 조회
    List<History> findBySeqUser(BookUser user);
    int countBySeqUserAndBookDtIsNotNull(BookUser user);
    int countBySeqUserAndBookDtIsNotNullAndReturnDtIsNotNull(BookUser user);
    int countBySeqUserAndBookDtIsNotNullAndReturnDtIsNull(BookUser user);
    int countBySeqUserAndReturnDtIsNullAndBookDtBefore(BookUser user, LocalDate localDate);
}
