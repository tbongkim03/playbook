package playbook.encore.back.history.service;

import playbook.encore.back.history.dto.HistoryBookResponseDto;
import playbook.encore.back.history.dto.PopularLabelDto;
import playbook.encore.back.history.dto.UserReadingRankDto;
import playbook.encore.back.bookUser.entity.BookUser;

import java.util.List;

public interface HistoryService {
    void handleBookBorrow(Object user, String barcodeBook, Integer campusId);
    void handleBookReturn(Object user, String barcodeBook, Integer campusId);

    HistoryBookResponseDto getHistoryBooks(Integer campusId);
    void deleteHistoryBook(int historyId);

    HistoryBookResponseDto getMyHistory(BookUser user);

    List<PopularLabelDto> findPopularFirstSortByCourse(int courseId);
    List<PopularLabelDto> findPopularFirstSortAll(Integer campusId);
    List<PopularLabelDto> findPopularSecondSortByCourse(int courseId);
    List<PopularLabelDto> findPopularSecondSortAll(Integer campusId);
    List<UserReadingRankDto> findUserReadingRankByCourse(int courseId);
    List<UserReadingRankDto> findUserReadingRankAll(Integer campusId);
}
