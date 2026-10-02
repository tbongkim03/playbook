package playbook.encore.back.history.dao;

import playbook.encore.back.history.dto.PopularLabelDto;
import playbook.encore.back.history.dto.RentalHistoryDto;
import playbook.encore.back.history.dto.RentalSummaryDto;
import playbook.encore.back.history.dto.UserReadingRankDto;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.history.entity.History;

import java.time.LocalDate;
import java.util.List;

public interface HistoryDAO {
    void bookBorrow(History history);
    void bookReturn(History history);

    RentalSummaryDto getRentalSummay();
    List<RentalHistoryDto> getRentalHistoryList();
    List<RentalHistoryDto> getRentalHistoryListByCampus(Integer campusId);
    List<RentalHistoryDto> getRentalHistoryListByDateRange(LocalDate startDate, LocalDate endDate);
    List<RentalHistoryDto> getRentalHistoryListByCampusAndDateRange(Integer campusId, LocalDate startDate, LocalDate endDate);

    void deleteHistory(History history);

    List<RentalHistoryDto> getMyRentalHistoryList(BookUser user);
    RentalSummaryDto getMyRentalSummay(BookUser user);

    List<PopularLabelDto> findPopularFirstSortByCourse(int courseId, LocalDate startDate, LocalDate endDate);
    List<PopularLabelDto> findPopularFirstSortAll(LocalDate startDate, LocalDate endDate);
    List<PopularLabelDto> findPopularSecondSortByCourse(int courseId, LocalDate startDate, LocalDate endDate);
    List<PopularLabelDto> findPopularSecondSortAll(LocalDate startDate, LocalDate endDate);
    List<UserReadingRankDto> findUserReadingRankByCourse(int courseId, LocalDate startDate, LocalDate endDate);
    List<UserReadingRankDto> findUserReadingRankAll(LocalDate startDate, LocalDate endDate);

    boolean existsByBookIdAndSeqUserAndReturnDateIsNull(int bookId, int userSeq);
}
