package playbook.encore.back.favor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.favor.dto.FavorResponseDto;
import playbook.encore.back.book.entity.Book;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.favor.entity.Favor;

import java.util.List;
import java.util.Optional;

public interface FavorRepository extends JpaRepository<Favor, Integer> {

    /** 사용자의 즐겨찾기 목록 조회 (도서 정보 포함 DTO 반환) */
    @Query("SELECT new playbook.encore.back.favor.dto.FavorResponseDto(f.seqFavor, b.seqBook, b.titleBook, b.authorBook) " +
            "FROM Favor f " +
            "JOIN f.seqBook b " +
            "WHERE f.seqUser = :user")
    List<FavorResponseDto> findFavorsByUser(@Param("user") BookUser user);

    /** 특정 사용자·도서 즐겨찾기 존재 여부 확인 */
    boolean existsBySeqUserAndSeqBook(BookUser user, Book book);

    /** 특정 사용자·도서 즐겨찾기 단건 조회 */
    Optional<Favor> findBySeqUserAndSeqBook(BookUser user, Book book);

    /** 특정 사용자·도서 즐겨찾기 소프트 삭제 */
    @Modifying
    @Query("UPDATE Favor f SET f.useYn = 'N' WHERE f.seqUser = :user AND f.seqBook = :book")
    void softDeleteBySeqUserAndSeqBook(@Param("user") BookUser user, @Param("book") Book book);

    /** 특정 사용자의 즐겨찾기 전체 소프트 삭제 (회원 탈퇴·삭제 시) */
    @Modifying
    @Query("UPDATE Favor f SET f.useYn = 'N' WHERE f.seqUser = :user")
    void softDeleteBySeqUser(@Param("user") BookUser user);

    /** 특정 도서를 즐겨찾기한 사용자 목록 조회 */
    @Query("SELECT f.seqUser FROM Favor f WHERE f.seqBook = :book")
    List<BookUser> findAllBySeqBook(@Param("book") Book book);
}
