package playbook.encore.back.history.dao;

import playbook.encore.back.history.dto.PopularLabelDto;
import playbook.encore.back.history.dto.RentalHistoryDto;
import playbook.encore.back.history.dto.RentalSummaryDto;
import playbook.encore.back.history.dto.UserReadingRankDto;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.history.entity.History;

import java.util.List;

public interface HistoryDAO {
    void bookBorrow(History history);
    void bookReturn(History history);

    RentalSummaryDto getRentalSummay();
    List<RentalHistoryDto> getRentalHistoryList();
    List<RentalHistoryDto> getRentalHistoryListByCampus(Integer campusId);

    void deleteHistory(History history);

    List<RentalHistoryDto> getMyRentalHistoryList(BookUser user);
    RentalSummaryDto getMyRentalSummay(BookUser user);

    List<PopularLabelDto> findPopularFirstSortByCourse(int courseId);
    List<PopularLabelDto> findPopularFirstSortAll();
    List<PopularLabelDto> findPopularSecondSortByCourse(int courseId);
    List<PopularLabelDto> findPopularSecondSortAll();
    List<UserReadingRankDto> findUserReadingRankByCourse(int courseId);
    List<UserReadingRankDto> findUserReadingRankAll();

    boolean existsByBookIdAndSeqUserAndReturnDateIsNull(int bookId, int userSeq);
}
