package playbook.encore.back.history.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import playbook.encore.back.history.dto.PopularLabelDto;
import playbook.encore.back.history.dto.UserReadingRankDto;
import playbook.encore.back.history.entity.History;
import playbook.encore.back.book.entity.Book;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.admin.entity.Admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Integer> {

    /** 사용자가 해당 도서를 현재 대출 중인지 확인 */
    boolean existsBySeqBookAndSeqUserAndReturnDtIsNull(Book book, BookUser user);

    /** 관리자가 해당 도서를 현재 대출 중인지 확인 */
    boolean existsBySeqBookAndSeqAdminAndReturnDtIsNull(Book book, Admin admin);

    /** 관리자의 미반납 대출 기록 단건 조회 (반납 처리용) */
    Optional<History> findBySeqBookAndSeqAdminAndReturnDtIsNull(Book book, Admin admin);

    /** 사용자의 미반납 대출 기록 단건 조회 (반납 처리용) */
    Optional<History> findBySeqBookAndSeqUserAndReturnDtIsNull(Book book, BookUser bookUser);

    /** 해당 도서가 현재 대출 중인지 확인 */
    boolean existsBySeqBookAndReturnDtIsNull(Book book);

    /** 사용자의 현재 대출 중인 도서 수 조회 */
    int countBySeqUserAndReturnDtIsNull(BookUser bookUser);

    /** 전체 대출 건수 (대시보드) */
    int countByBookDtIsNotNull();

    /** 전체 반납 완료 건수 (대시보드) */
    int countByBookDtIsNotNullAndReturnDtIsNotNull();

    /** 전체 대출 중 건수 (대시보드) */
    int countByBookDtIsNotNullAndReturnDtIsNull();

    /** 전체 연체 건수 (대시보드) */
    int countByReturnDtIsNullAndBookDtBefore(LocalDate localDate);

    /** 사용자의 미반납 대출 목록 전체 조회 (연체 여부 판단용) */
    List<History> findAllBySeqUserAndReturnDtIsNull(BookUser bookUser);

    /** 사용자의 전체 대출 기록 조회 */
    List<History> findBySeqUser(BookUser user);

    /** 사용자의 전체 대출 건수 */
    int countBySeqUserAndBookDtIsNotNull(BookUser user);

    /** 사용자의 반납 완료 건수 */
    int countBySeqUserAndBookDtIsNotNullAndReturnDtIsNotNull(BookUser user);

    /** 사용자의 현재 대출 중 건수 */
    int countBySeqUserAndBookDtIsNotNullAndReturnDtIsNull(BookUser user);

    /** 사용자의 연체 건수 */
    int countBySeqUserAndReturnDtIsNullAndBookDtBefore(BookUser user, LocalDate localDate);

    /** 인기 대분류 조회 (특정 과정) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(sf.korSortFirst, COUNT(h))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        JOIN ss.seqSortFirst sf
        WHERE h.seqCourse.seqCourse = :courseId
        GROUP BY sf.seqSortFirst, sf.nameSortFirst
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortByCourse(@Param("courseId") int courseId);


    /** 인기 대분류 조회 (전체 과정) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(sf.korSortFirst, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        JOIN ss.seqSortFirst sf
        WHERE h.seqUser IS NOT NULL
        GROUP BY sf.seqSortFirst, sf.nameSortFirst
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortAll();

    /** 인기 소분류 조회 (특정 과정) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        WHERE h.seqCourse.seqCourse = :courseId
        GROUP BY ss.seqSortSecond, ss.nameSortSecond
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortByCourse(@Param("courseId") int courseId);

    /** 인기 소분류 조회 (전체 과정) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        WHERE h.seqUser IS NOT NULL
        GROUP BY ss.seqSortSecond, ss.nameSortSecond
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortAll();

    /** 회원 다독 순위 조회 (특정 과정) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqUser u
        WHERE h.seqCourse.seqCourse = :courseId AND h.returnDt IS NOT NULL
        GROUP BY u.seqUser, u.nameUser
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankByCourse(@Param("courseId") int courseId);

    /** 회원 다독 순위 조회 (전체 과정) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqUser u
        WHERE h.returnDt IS NOT NULL
        GROUP BY u.seqUser, u.nameUser
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankAll();

    boolean existsBySeqBook_SeqBookAndSeqUser_SeqUserAndReturnDtIsNull(int bookId, int userSeq);

    /** 인기 대분류 (전체, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(sf.korSortFirst, COUNT(h.seqHistory))
        FROM History h JOIN h.seqBook b JOIN b.seqSortSecond ss JOIN ss.seqSortFirst sf
        WHERE h.seqUser IS NOT NULL AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY sf.seqSortFirst, sf.nameSortFirst ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortAllByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 인기 대분류 (캠퍼스, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(sf.korSortFirst, COUNT(h.seqHistory))
        FROM History h JOIN h.seqBook b JOIN b.seqSortSecond ss JOIN ss.seqSortFirst sf
        WHERE h.seqUser IS NOT NULL AND h.seqCampus.seqCampus = :campusId AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY sf.seqSortFirst, sf.nameSortFirst ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortAllByCampusAndDateRange(@Param("campusId") int campusId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 인기 대분류 (특정 과정, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(sf.korSortFirst, COUNT(h))
        FROM History h JOIN h.seqBook b JOIN b.seqSortSecond ss JOIN ss.seqSortFirst sf
        WHERE h.seqCourse.seqCourse = :courseId AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY sf.seqSortFirst, sf.nameSortFirst ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortByCourseAndDateRange(@Param("courseId") int courseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 인기 중분류 (전체, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h JOIN h.seqBook b JOIN b.seqSortSecond ss
        WHERE h.seqUser IS NOT NULL AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY ss.seqSortSecond, ss.nameSortSecond ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortAllByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 인기 중분류 (캠퍼스, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h JOIN h.seqBook b JOIN b.seqSortSecond ss
        WHERE h.seqUser IS NOT NULL AND h.seqCampus.seqCampus = :campusId AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY ss.seqSortSecond, ss.nameSortSecond ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortAllByCampusAndDateRange(@Param("campusId") int campusId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 인기 중분류 (특정 과정, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h JOIN h.seqBook b JOIN b.seqSortSecond ss
        WHERE h.seqCourse.seqCourse = :courseId AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY ss.seqSortSecond, ss.nameSortSecond ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortByCourseAndDateRange(@Param("courseId") int courseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 다독 순위 (전체, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h JOIN h.seqUser u
        WHERE h.returnDt IS NOT NULL AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY u.seqUser, u.nameUser ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankAllByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 다독 순위 (캠퍼스, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h JOIN h.seqUser u
        WHERE h.returnDt IS NOT NULL AND h.seqCampus.seqCampus = :campusId AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY u.seqUser, u.nameUser ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankAllByCampusAndDateRange(@Param("campusId") int campusId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 다독 순위 (특정 과정, 날짜 범위) */
    @Query("""
        SELECT new playbook.encore.back.history.dto.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h JOIN h.seqUser u
        WHERE h.seqCourse.seqCourse = :courseId AND h.returnDt IS NOT NULL AND h.bookDt BETWEEN :startDate AND :endDate
        GROUP BY u.seqUser, u.nameUser ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankByCourseAndDateRange(@Param("courseId") int courseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM History h WHERE h.bookDt = :bookDate AND h.returnDt IS NULL")
    List<History> findByBookDtAndReturnDtIsNull(@Param("bookDate") LocalDate bookDate);

    @Query("SELECT h FROM History h WHERE h.bookDt < :currentDate AND h.returnDt IS NULL")
    List<History> findOverdueBooks(@Param("currentDate") LocalDate currentDate);

    List<History> findAllBySeqAdminAndReturnDtIsNull(Admin admin);

    boolean existsBySeqUserAndReturnDtIsNull(BookUser bookUser);

    // 도서별 대출 횟수 조회
    int countBySeqBook(Book book);

    // ========== 캠퍼스 필터링 메서드 (추가) ==========

    /**
     * 캠퍼스별 인기 대분류 (특정 과정)
     */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(sf.korSortFirst, COUNT(h))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        JOIN ss.seqSortFirst sf
        WHERE h.seqCourse.seqCourse = :courseId
          AND h.seqCampus.seqCampus = :campusId
        GROUP BY sf.seqSortFirst, sf.nameSortFirst
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortByCourseAndCampus(
        @Param("courseId") int courseId,
        @Param("campusId") int campusId);

    /**
     * 캠퍼스별 인기 대분류 (전체 과정)
     */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(sf.korSortFirst, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        JOIN ss.seqSortFirst sf
        WHERE h.seqUser IS NOT NULL
          AND h.seqCampus.seqCampus = :campusId
        GROUP BY sf.seqSortFirst, sf.nameSortFirst
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularFirstSortAllByCampus(@Param("campusId") int campusId);

    /**
     * 캠퍼스별 인기 중분류 (특정 과정)
     */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        WHERE h.seqCourse.seqCourse = :courseId
          AND h.seqCampus.seqCampus = :campusId
        GROUP BY ss.seqSortSecond, ss.nameSortSecond
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortByCourseAndCampus(
        @Param("courseId") int courseId,
        @Param("campusId") int campusId);

    /**
     * 캠퍼스별 인기 중분류 (전체 과정)
     */
    @Query("""
        SELECT new playbook.encore.back.history.dto.PopularLabelDto(ss.korSortSecond, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqBook b
        JOIN b.seqSortSecond ss
        WHERE h.seqUser IS NOT NULL
          AND h.seqCampus.seqCampus = :campusId
        GROUP BY ss.seqSortSecond, ss.nameSortSecond
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<PopularLabelDto> findPopularSecondSortAllByCampus(@Param("campusId") int campusId);

    /**
     * 캠퍼스별 다독 순위 (특정 과정)
     */
    @Query("""
        SELECT new playbook.encore.back.history.dto.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqUser u
        WHERE h.seqCourse.seqCourse = :courseId
          AND h.returnDt IS NOT NULL
          AND h.seqCampus.seqCampus = :campusId
        GROUP BY u.seqUser, u.nameUser
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankByCourseAndCampus(
        @Param("courseId") int courseId,
        @Param("campusId") int campusId);

    /**
     * 캠퍼스별 다독 순위 (전체 과정)
     */
    @Query("""
        SELECT new playbook.encore.back.history.dto.UserReadingRankDto(u.nameUser, COUNT(h.seqHistory))
        FROM History h
        JOIN h.seqUser u
        WHERE h.returnDt IS NOT NULL
          AND h.seqCampus.seqCampus = :campusId
        GROUP BY u.seqUser, u.nameUser
        ORDER BY COUNT(h.seqHistory) DESC
    """)
    List<UserReadingRankDto> findUserReadingRankAllByCampus(@Param("campusId") int campusId);

    /**
     * 캠퍼스별 반납 예정 도서 (스케줄러용)
     */
    @Query("SELECT h FROM History h WHERE h.bookDt = :bookDate " +
           "AND h.returnDt IS NULL AND h.seqCampus.seqCampus = :campusId")
    List<History> findByBookDtAndReturnDtIsNullAndSeqCampus_SeqCampus(
        @Param("bookDate") LocalDate bookDate,
        @Param("campusId") Integer campusId);

    /**
     * 캠퍼스별 연체 도서 (스케줄러용)
     */
    @Query("SELECT h FROM History h WHERE h.bookDt < :currentDate " +
           "AND h.returnDt IS NULL AND h.seqCampus.seqCampus = :campusId")
    List<History> findOverdueBooksByCampus(
        @Param("currentDate") LocalDate currentDate,
        @Param("campusId") Integer campusId);

    /**
     * 캠퍼스별 대시보드 통계
     */
    int countByBookDtIsNotNullAndSeqCampus_SeqCampus(Integer campusId);
    int countByBookDtIsNotNullAndReturnDtIsNotNullAndSeqCampus_SeqCampus(Integer campusId);
    int countByBookDtIsNotNullAndReturnDtIsNullAndSeqCampus_SeqCampus(Integer campusId);
    int countByReturnDtIsNullAndBookDtBeforeAndSeqCampus_SeqCampus(LocalDate localDate, Integer campusId);

    /**
     * 캠퍼스별 히스토리 조회
     */
    List<History> findBySeqCampus_SeqCampus(Integer campusId);

    /**
     * 날짜 범위 히스토리 조회
     */
    List<History> findByBookDtBetween(LocalDate startDate, LocalDate endDate);
    List<History> findBySeqCampus_SeqCampusAndBookDtBetween(Integer campusId, LocalDate startDate, LocalDate endDate);
}
