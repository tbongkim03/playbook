package playbook.encore.back.service;

import playbook.encore.back.data.dto.history.HistoryBookResponseDto;

public interface HistoryService {
    void handleBookBorrow(Object user, String barcodeBook);
    void handleBookReturn(Object user, String barcodeBook);

    HistoryBookResponseDto getHistoryBooks();
    void deleteHistoryBook(int historyId);
}
