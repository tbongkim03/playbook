package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.data.dto.favor.FavorResponseDto;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.Favor;

import java.util.List;
import java.util.Optional;

public interface FavorRepository extends JpaRepository<Favor, Integer> {
    @Query("SELECT new playbook.encore.back.data.dto.favor.FavorResponseDto(f.seqFavor, b.seqBook, b.titleBook, b.authorBook) " +
            "FROM Favor f " +
            "JOIN f.seqBook b " +
            "WHERE f.seqUser = :user")
    List<FavorResponseDto> findFavorsByUser(@Param("user") BookUser user);

    boolean existsBySeqUserAndSeqBook(BookUser user, Book book);
    Optional<Favor> findBySeqUserAndSeqBook(BookUser user, Book book);

    void deleteBySeqUserAndSeqBook(BookUser user, Book book);

    void deleteBySeqUser(BookUser user);
}
