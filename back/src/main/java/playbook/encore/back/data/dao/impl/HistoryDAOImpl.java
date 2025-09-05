package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import playbook.encore.back.data.dao.HistoryDAO;
import playbook.encore.back.data.dto.history.PopularLabelDto;
import playbook.encore.back.data.dto.history.RentalHistoryDto;
import playbook.encore.back.data.dto.history.RentalSummaryDto;
import playbook.encore.back.data.dto.history.UserReadingRankDto;
import playbook.encore.back.data.entity.BookUser;
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
            if (history.getSeqBook() == null) {
                continue; // 책 정보가 없으면 스킵
            }

            String bookTitle = history.getSeqBook().getTitleBook();
            String bookAuthor = history.getSeqBook().getAuthorBook();
            String bookIsbn = history.getSeqBook().getIsbnBook();

            // 유저 이름: 일반 유저가 있으면 그 이름, 없으면 관리자 이름
            String userName;
            String userId;
            String courseName = null;

            if (history.getSeqUser() != null) {
                userName = history.getSeqUser().getNameUser();
                userId = history.getSeqUser().getIdUser();
                courseName = history.getSeqUser().getSeqCourse() != null ?
                        history.getSeqUser().getSeqCourse().getNameCourse() : "종료된 과정";
            } else if (history.getSeqAdmin() != null) {
                userName = history.getSeqAdmin().getNameAdmin();
                userId = history.getSeqAdmin().getIdAdmin();
            } else {
                // 둘 다 NULL인 경우
                userName = "탈퇴 사용자";
                userId = "unknown";
            }

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
                    courseName,
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

    @Override
    public List<RentalHistoryDto> getMyRentalHistoryList(BookUser user) {
        List<History> historyList = historyRepository.findBySeqUser(user);
        List<RentalHistoryDto> rentalHistoryDtoList = new ArrayList<>();

        for (History history : historyList) {
            if (history.getSeqBook() == null) {
                continue;
            }

            String bookTitle = history.getSeqBook().getTitleBook();
            String bookAuthor = history.getSeqBook().getAuthorBook();
            String bookIsbn = history.getSeqBook().getIsbnBook();

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

            String courseName = user.getSeqCourse() != null ?
                    user.getSeqCourse().getNameCourse() : "종료한 과정";

            RentalHistoryDto rentalHistoryDto = new RentalHistoryDto(
                    bookTitle,
                    bookAuthor,
                    bookIsbn,
                    user.getNameUser(),
                    user.getIdUser(),
                    courseName,
                    borrowDate,
                    returnDate,
                    status.toString()
            );

            rentalHistoryDtoList.add(rentalHistoryDto);
        }

        return rentalHistoryDtoList;
    }

    @Override
    public RentalSummaryDto getMyRentalSummay(BookUser user) {
        // 총 대여 수
        int totalBorrowed = historyRepository.countBySeqUserAndBookDtIsNotNull(user);
        // 총 반납 수
        int totalReturned = historyRepository.countBySeqUserAndBookDtIsNotNullAndReturnDtIsNotNull(user);
        // 현재 대여 수
        int currentlyBorrowed = historyRepository.countBySeqUserAndBookDtIsNotNullAndReturnDtIsNull(user);
        // 총 연체 수
        int overdueCount = historyRepository.countBySeqUserAndReturnDtIsNullAndBookDtBefore(user, LocalDate.now().minusDays(7));

        return new RentalSummaryDto(totalBorrowed, totalReturned, currentlyBorrowed, overdueCount);
    }

    @Override
    public List<PopularLabelDto> findPopularFirstSortByCourse(int courseId) {
        return historyRepository.findPopularFirstSortByCourse(courseId);
    }

    @Override
    public List<PopularLabelDto> findPopularFirstSortAll() {
        return historyRepository.findPopularFirstSortAll();
    }

    @Override
    public List<PopularLabelDto> findPopularSecondSortAll() {
        return historyRepository.findPopularSecondSortAll();
    }

    @Override
    public List<PopularLabelDto> findPopularSecondSortByCourse(int courseId) {
        return historyRepository.findPopularSecondSortByCourse(courseId);
    }

    @Override
    public List<UserReadingRankDto> findUserReadingRankByCourse(int courseId) {
        return historyRepository.findUserReadingRankByCourse(courseId);
    }

    @Override
    public List<UserReadingRankDto> findUserReadingRankAll() {
        return historyRepository.findUserReadingRankAll();
    }

    @Override
    public boolean existsByBookIdAndSeqUserAndReturnDateIsNull(int bookId, int userSeq) {
        boolean result = historyRepository.existsBySeqBook_SeqBookAndSeqUser_SeqUserAndReturnDtIsNull(bookId, userSeq);
        return result;
    }
}
