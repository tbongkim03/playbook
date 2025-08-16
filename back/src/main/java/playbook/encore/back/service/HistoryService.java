package playbook.encore.back.service;

import playbook.encore.back.data.dto.history.HistoryBookResponseDto;
import playbook.encore.back.data.entity.BookUser;

public interface HistoryService {
    void handleBookBorrow(Object user, String barcodeBook);
    void handleBookReturn(Object user, String barcodeBook);

    HistoryBookResponseDto getHistoryBooks();
    void deleteHistoryBook(int historyId);

    HistoryBookResponseDto getMyHistory(BookUser user);
}
