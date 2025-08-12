package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import playbook.encore.back.data.dao.HistoryDAO;
import playbook.encore.back.data.dto.history.RentalHistoryDto;
import playbook.encore.back.data.dto.history.RentalSummaryDto;
import playbook.encore.back.data.entity.History;
import playbook.encore.back.data.repository.HistoryRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class HistoryDAOImpl implements HistoryDAO {

    private final HistoryRepository historyRepository;

    @Autowired
    public HistoryDAOImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public void bookBorrow(History history) {
        historyRepository.save(history);
    }

    @Override
    public void bookReturn(History history) {
        historyRepository.save(history);
    }

    @Override
    public RentalSummaryDto getRentalSummay() {
        // 총 대여 수
        int totalBorrowed = historyRepository.countByBookDtIsNotNull();
        // 총 반납 수
        int totalReturned = historyRepository.countByBookDtIsNotNullAndReturnDtIsNotNull();
        // 현재 대여 수
        int currentlyBorrowed = historyRepository.countByBookDtIsNotNullAndReturnDtIsNull();
        // 총 연체 수
        int overdueCount = historyRepository.countByReturnDtIsNullAndBookDtBefore(LocalDate.now().minusDays(7));

        return new RentalSummaryDto(totalBorrowed, totalReturned, currentlyBorrowed, overdueCount);
    }

    @Override
    public List<RentalHistoryDto> getRentalHistoryList() {
        List<History> historyList = historyRepository.findAll();
        List<RentalHistoryDto> rentalHistoryDtoList = new ArrayList<>();

        for (History history : historyList) {
            String bookTitle = history.getSeqBook().getTitleBook();
            String bookAuthor = history.getSeqBook().getAuthorBook();
            String bookIsbn = history.getSeqBook().getIsbnBook();

            // 유저 이름: 일반 유저가 있으면 그 이름, 없으면 관리자 이름
            String userName = history.getSeqUser() != null
                    ? history.getSeqUser().getNameUser()
                    : history.getSeqAdmin().getNameAdmin();

            String userId = history.getSeqUser() != null
                    ? history.getSeqUser().getIdUser()
                    : history.getSeqAdmin().getIdAdmin(); 

            LocalDate borrowDate = history.getBookDt();
            LocalDate returnDate = history.getReturnDt();

            // 상태 결정
            LocalDate dueDate = borrowDate.plusDays(7);
            History.StatusType status;

            if (returnDate != null) {
                status = History.StatusType.returned;
            } else if (dueDate.isBefore(LocalDate.now())) {
                status = History.StatusType.overdue;
            } else {
                status = History.StatusType.booked;
            }

            RentalHistoryDto rentalHistoryDto = new RentalHistoryDto(
                    bookTitle,
                    bookAuthor,
                    bookIsbn,
                    userName,
                    userId,
                    borrowDate,
                    returnDate,
                    status.toString()
            );

            rentalHistoryDtoList.add(rentalHistoryDto);
        }

        return rentalHistoryDtoList;
    }

    @Override
    public void deleteHistory(History history) {
        historyRepository.delete(history);
    }

}
