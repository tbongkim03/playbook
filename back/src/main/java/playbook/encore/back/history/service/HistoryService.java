package playbook.encore.back.history.service;

import playbook.encore.back.history.dto.BookReturnResultDto;
import playbook.encore.back.history.dto.HistoryBookResponseDto;
import playbook.encore.back.history.dto.PopularLabelDto;
import playbook.encore.back.history.dto.UserReadingRankDto;
import playbook.encore.back.bookUser.entity.BookUser;

import java.time.LocalDate;
import java.util.List;

public interface HistoryService {
    void handleBookBorrow(Object user, String barcodeBook, Integer campusId);

    /**
     * 도서 반납 처리.
     *
     * <p>연체는 예외가 아니라 반환값({@link BookReturnResultDto})으로 알린다.
     * 예외를 던지면 {@code rollbackFor = Exception.class}에 걸려 반납 자체가 취소되기 때문이다.
     * 반납 불가 상황(대여 기록 없음, 캠퍼스/바코드 불일치 등)은 기존대로 예외를 던져 롤백한다.</p>
     */
    BookReturnResultDto handleBookReturn(Object user, String barcodeBook, Integer campusId);

    HistoryBookResponseDto getHistoryBooks(Integer campusId);
    HistoryBookResponseDto getHistoryBooks(Integer campusId, LocalDate startDate, LocalDate endDate);
    void deleteHistoryBook(int historyId);

    HistoryBookResponseDto getMyHistory(BookUser user);

    List<PopularLabelDto> findPopularFirstSortByCourse(int courseId, LocalDate startDate, LocalDate endDate);
    List<PopularLabelDto> findPopularFirstSortAll(Integer campusId, LocalDate startDate, LocalDate endDate);
    List<PopularLabelDto> findPopularSecondSortByCourse(int courseId, LocalDate startDate, LocalDate endDate);
    List<PopularLabelDto> findPopularSecondSortAll(Integer campusId, LocalDate startDate, LocalDate endDate);
    List<UserReadingRankDto> findUserReadingRankByCourse(int courseId, LocalDate startDate, LocalDate endDate);
    List<UserReadingRankDto> findUserReadingRankAll(Integer campusId, LocalDate startDate, LocalDate endDate);

    byte[] exportExcel(Integer campusId) throws java.io.IOException;
}
