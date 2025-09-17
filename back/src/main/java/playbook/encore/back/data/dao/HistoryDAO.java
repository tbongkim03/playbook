package playbook.encore.back.data.dao;

import playbook.encore.back.data.dto.history.PopularLabelDto;
import playbook.encore.back.data.dto.history.RentalHistoryDto;
import playbook.encore.back.data.dto.history.RentalSummaryDto;
import playbook.encore.back.data.dto.history.UserReadingRankDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.History;

import java.util.List;

public interface HistoryDAO {
    void bookBorrow(History history);
    void bookReturn(History history);

    RentalSummaryDto getRentalSummay();
    List<RentalHistoryDto> getRentalHistoryList();

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
