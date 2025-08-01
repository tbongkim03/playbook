package playbook.encore.back.service;

public interface HistoryService {
    void handleBookBorrow(Object user, String barcodeBook);
    void handleBookReturn(Object user, String barcodeBook);
}
