package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.data.dto.history.PopularLabelDto;
import playbook.encore.back.data.dto.history.UserReadingRankDto;
import playbook.encore.back.data.entity.*;

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

    // 1. 인기 대분류 (특정 과정)
    @Query("""
        SELECT new playbook.encore.back.data.dto.history.PopularLabelDto(sf.korSortFirst, COUNT(h))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        JOIN ss.seqSortFirst sf
        WHERE h.seqCourse.seqCourse = :courseId
        GROUP BY sf.seqSortFirst, sf.nameSortFirst
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortByCourse(@Param("courseId") int courseId);


    // 1-1. 인기 대분류 (전체 과정)
    @Query("""
        SELECT new playbook.encore.back.data.dto.history.PopularLabelDto(sf.korSortFirst, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        JOIN ss.seqSortFirst sf
        GROUP BY sf.seqSortFirst, sf.nameSortFirst
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortAll();

    // 2. 인기 중분류 (특정 과정)
    @Query("""
        SELECT new playbook.encore.back.data.dto.history.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        WHERE h.seqCourse.seqCourse = :courseId
        GROUP BY ss.seqSortSecond, ss.nameSortSecond
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortByCourse(@Param("courseId") int courseId);

    // 2-1. 인기 중분류 (전체 과정)
    @Query("""
        SELECT new playbook.encore.back.data.dto.history.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        GROUP BY ss.seqSortSecond, ss.nameSortSecond
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortAll();

    // 3. 회원 다독 순위 (특정 과정)
    @Query("""
        SELECT new playbook.encore.back.data.dto.history.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqUser u
        WHERE h.seqCourse.seqCourse = :courseId AND h.returnDt IS NOT NULL
        GROUP BY u.seqUser, u.nameUser
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankByCourse(@Param("courseId") int courseId);

    // 3-1. 회원 다독 순위 (전체 과정)
    @Query("""
        SELECT new playbook.encore.back.data.dto.history.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqUser u
        WHERE h.returnDt IS NOT NULL
        GROUP BY u.seqUser, u.nameUser
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankAll();

    boolean existsBySeqBook_SeqBookAndSeqUser_SeqUserAndReturnDtIsNull(int bookId, int userSeq);

    @Query("SELECT h FROM History h WHERE h.bookDt = :bookDate AND h.returnDt IS NULL")
    List<History> findByBookDtAndReturnDtIsNull(@Param("bookDate") LocalDate bookDate);

    @Query("SELECT h FROM History h WHERE h.bookDt < :overdueDate AND h.returnDt IS NULL")
    List<History> findOverdueBooks(@Param("overdueDate") LocalDate overdueDate);

    List<History> findBySeqCourseAndReturnDtIsNull(Course course);
}
