package playbook.encore.back.service;

import playbook.encore.back.data.dto.history.HistoryBookResponseDto;
import playbook.encore.back.data.dto.history.PopularLabelDto;
import playbook.encore.back.data.dto.history.UserReadingRankDto;
import playbook.encore.back.data.entity.BookUser;

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
