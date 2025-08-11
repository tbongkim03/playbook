package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.HistoryDAO;
import playbook.encore.back.data.dto.history.HistoryBookResponseDto;
import playbook.encore.back.data.dto.history.RentalHistoryDto;
import playbook.encore.back.data.dto.history.RentalSummaryDto;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.History;
import playbook.encore.back.data.repository.*;
import playbook.encore.back.service.HistoryService;

import java.time.LocalDate;
import java.util.List;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryDAO historyDAO;
    private final AdminRepository adminRepository;
    private final BookUserRepository bookUserRepository;
    private final BookRepository bookRepository;
    private final HistoryRepository historyRepository;

    @Autowired
    public HistoryServiceImpl(HistoryDAO historyDAO, AdminRepository adminRepository, BookUserRepository bookUserRepository, BookRepository bookRepository, HistoryRepository historyRepository) {
        this.historyDAO = historyDAO;
        this.adminRepository = adminRepository;
        this.bookUserRepository = bookUserRepository;
        this.bookRepository = bookRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional
    public void handleBookBorrow(Object human, String barcodeBook) {
        Object user;
        boolean isAdmin = false;

        if (human instanceof BookUser bookUser) {
            user = bookUserRepository.findByIdUser(bookUser.getIdUser())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        } else if (human instanceof Admin admin) {
            user = adminRepository.findByIdAdmin(admin.getIdAdmin())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운영자입니다."));
            isAdmin = true;
        } else {
            throw new IllegalArgumentException("잘못된 사용자 타입입니다.");
        }

        Book book = bookRepository.findByBarcodeBook(barcodeBook)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 바코드입니다."));

        boolean bookAlreadyBorrowed = historyRepository.existsBySeqBookAndReturnDtIsNull(book);
        if (bookAlreadyBorrowed) {
            throw new IllegalArgumentException("이미 대여중인 도서입니다.");
        }

        // 동일인이 대여하는 경우
        if (isAdmin) {
            Admin admin = (Admin) user;
            boolean alreadyBorrowed = historyRepository.existsBySeqBookAndSeqAdminAndReturnDtIsNull(book, admin);
            if (alreadyBorrowed) {
                throw new IllegalArgumentException("이미 대여중인 도서입니다.");
            }
        } else {
            BookUser bookUser = (BookUser) user;

            // 연체 여부 확인
            List<History> unreturnedHistories = historyRepository.findAllBySeqUserAndReturnDtIsNull(bookUser);
            boolean hasOverdue = unreturnedHistories.stream().anyMatch(this::isOverdue);
            if (hasOverdue) {
                throw new IllegalArgumentException("연체 중인 도서가 있어 대출할 수 없습니다.");
            }

            int borrowedCount = historyRepository.countBySeqUserAndReturnDtIsNull(bookUser);
            if (borrowedCount > 2) {
                throw new IllegalArgumentException("최대 2권까지 대여할 수 있습니다.");
            }

            boolean alreadyBorrowed = historyRepository.existsBySeqBookAndSeqUserAndReturnDtIsNull(book, bookUser);
            if (alreadyBorrowed) {
                throw new IllegalArgumentException("이미 대여중인 도서입니다.");
            }
        }

        History.HistoryBuilder historyBuilder = History.builder()
                .seqBook(book)
                .bookDt(LocalDate.now())
                .returnDt(null); // 반납 전 null

        if (isAdmin) {
            Admin admin = (Admin) user;
            historyBuilder.seqAdmin(admin).seqUser(null).seqCourse(null);
        } else {
            BookUser bookUser = (BookUser) user;
            historyBuilder.seqUser(bookUser).seqCourse(bookUser.getSeqCourse()).seqAdmin(null);
        }

        History history = historyBuilder.build();
        historyDAO.bookBorrow(history);
    }


    @Override
    @Transactional
    public void handleBookReturn(Object human, String barcodeBook) {
        Object user;
        boolean isAdmin = false;

        if (human instanceof BookUser bookUser) {
            user = bookUserRepository.findByIdUser(bookUser.getIdUser())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        } else if (human instanceof Admin admin) {
            user = adminRepository.findByIdAdmin(admin.getIdAdmin())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운영자입니다."));
            isAdmin = true;
        } else {
            throw new IllegalArgumentException("잘못된 사용자 타입입니다.");
        }

        Book book = bookRepository.findByBarcodeBook(barcodeBook)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 바코드입니다."));

        History history;

        if (isAdmin) {
            Admin admin = (Admin) user;
            history = historyRepository
                    .findBySeqBookAndSeqAdminAndReturnDtIsNull(book, admin)
                    .orElseThrow(() -> new IllegalArgumentException("반납할 대여 기록이 없습니다."));
        } else {
            BookUser bookUser = (BookUser) user;
            history = historyRepository
                    .findBySeqBookAndSeqUserAndReturnDtIsNull(book, bookUser)
                    .orElseThrow(() -> new IllegalArgumentException("반납할 대여 기록이 없습니다."));
        }

        LocalDate today = LocalDate.now();
        history.setReturnDt(today);
        historyDAO.bookReturn(history);

        if (isOverdue(history)) {
            long overdueDays = LocalDate.now().toEpochDay() - history.getBookDt().plusDays(7).toEpochDay();
            throw new IllegalArgumentException("연체 반납되었습니다. 연체일수: " + overdueDays + "일");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryBookResponseDto getHistoryBooks() {
        RentalSummaryDto rentalSummaryDto = historyDAO.getRentalSummay();
        List<RentalHistoryDto> rentalHistoryDtoList = historyDAO.getRentalHistoryList();

        return new HistoryBookResponseDto(rentalSummaryDto, rentalHistoryDtoList);
    }

    @Override
    public void deleteHistoryBook(int historyId) {
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대여 기록입니다."));
        historyDAO.deleteHistory(history);
    }

    // 연체 인지
    private boolean isOverdue(History history) {
        return history.getReturnDt() == null && history.getBookDt().isBefore(LocalDate.now().minusDays(7));
    }


}
