package playbook.encore.back.data.dao;

import playbook.encore.back.data.dto.history.RentalHistoryDto;
import playbook.encore.back.data.dto.history.RentalSummaryDto;
import playbook.encore.back.data.entity.History;

import java.util.List;

public interface HistoryDAO {
    void bookBorrow(History history);
    void bookReturn(History history);

    RentalSummaryDto getRentalSummay();
    List<RentalHistoryDto> getRentalHistoryList();
}
