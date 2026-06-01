package playbook.encore.back.history.service;

import playbook.encore.back.history.dto.HistoryBookResponseDto;
import playbook.encore.back.history.dto.PopularLabelDto;
import playbook.encore.back.history.dto.UserReadingRankDto;
import playbook.encore.back.bookUser.entity.BookUser;

import java.time.LocalDate;
import java.util.List;

public interface HistoryService {
    void handleBookBorrow(Object user, String barcodeBook, Integer campusId);
    void handleBookReturn(Object user, String barcodeBook, Integer campusId);

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
