package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.AdminDAO;
import playbook.encore.back.data.dao.BookDAO;
import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.data.dao.HistoryDAO;
import playbook.encore.back.data.dto.history.*;
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
    private final AdminDAO adminDAO;
    private final BookUserDAO bookUserDAO;
    private final CourseRepository courseRepository;
    private final BookDAO bookDAO;

    @Autowired
    public HistoryServiceImpl(HistoryDAO historyDAO, AdminRepository adminRepository, BookUserRepository bookUserRepository, BookRepository bookRepository, HistoryRepository historyRepository, AdminDAO adminDAO, BookUserDAO bookUserDAO, CourseRepository courseRepository, BookDAO bookDAO) {
        this.historyDAO = historyDAO;
        this.adminRepository = adminRepository;
        this.bookUserRepository = bookUserRepository;
        this.bookRepository = bookRepository;
        this.historyRepository = historyRepository;
        this.adminDAO = adminDAO;
        this.bookUserDAO = bookUserDAO;
        this.courseRepository = courseRepository;
        this.bookDAO = bookDAO;
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

        // 사용자 상태 확인 - 대여 가능 여부 체크
        if (isAdmin) {
            Admin admin = (Admin) user;
            Admin.StatusTypeAdmin currentStatus = admin.getStatusAdmin();
            if (currentStatus == Admin.StatusTypeAdmin.overdue || currentStatus == Admin.StatusTypeAdmin.stop) {
                throw new IllegalArgumentException("대여 불가능한 상태입니다. 현재 상태: " + currentStatus);
            }
        } else {
            BookUser bookUser = (BookUser) user;
            BookUser.StatusType currentStatus = bookUser.getStatusUser();
            if (currentStatus == BookUser.StatusType.overdue || currentStatus == BookUser.StatusType.stop) {
                throw new IllegalArgumentException("대여 불가능한 상태입니다. 현재 상태: " + currentStatus);
            }
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
            if (borrowedCount >= 2) {
                throw new IllegalArgumentException("최대 2권까지 대여할 수 있습니다.");
            }

            boolean alreadyBorrowed = historyRepository.existsBySeqBookAndSeqUserAndReturnDtIsNull(book, bookUser);
            if (alreadyBorrowed) {
                throw new IllegalArgumentException("이미 대여중인 도서입니다.");
            }
        }

        // 대여 기록 생성
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

        try {
            // 대여 기록 저장 후 상태 업데이트
            Book borrowBook = bookDAO.bookStatusUpdate(book, true);
        } catch (Exception e) {
            // 대여 기록 저장 실패 시 롤백
            throw new RuntimeException(e.getMessage());
        }

        // 대여 후 상태 업데이트
        if (isAdmin) {
            Admin admin = (Admin) user;
            // Admin은 대여 권수에 관계없이 available 상태 유지
            Admin.StatusTypeAdmin status = Admin.StatusTypeAdmin.available;
            adminDAO.updateStatus(admin, status);
        } else {
            BookUser bookUser = (BookUser) user;
            int currentBorrowedCount = historyRepository.countBySeqUserAndReturnDtIsNull(bookUser);

            BookUser.StatusType status;
            if (currentBorrowedCount >= 2) {
                // 2권 대여 시 stop 상태로 변경
                status = BookUser.StatusType.stop;
            } else {
                // 1권 대여 시 available 상태 유지
                status = BookUser.StatusType.available;
            }
            bookUserDAO.updateStatus(bookUser, status);
        }
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

        try {
            // 반납 기록 저장 후 상태 업데이트
            Book borrowBook = bookDAO.bookStatusUpdate(book, false);
        } catch (Exception e) {
            // 반납 기록 저장 실패 시 롤백
            throw new RuntimeException(e.getMessage());
        }

        // 반납한 도서가 연체인지 먼저 확인
        boolean isReturnedBookOverdue = isOverdue(history);

        // 상태 업데이트 (연체 여부에 관계없이 한 번만 처리)
        if (isAdmin) {
            Admin admin = (Admin) user;
            Admin.StatusTypeAdmin status = isReturnedBookOverdue ?
                    Admin.StatusTypeAdmin.overdue : Admin.StatusTypeAdmin.available;
            adminDAO.updateStatus(admin, status);
        } else {
            BookUser bookUser = (BookUser) user;

            // 반납 후 현재 대여 중인 도서 수 확인
            int remainingBorrowedCount = historyRepository.countBySeqUserAndReturnDtIsNull(bookUser);

            // 나머지 대여 중인 도서들 중 연체된 것이 있는지 확인
            List<History> remainingHistories = historyRepository.findAllBySeqUserAndReturnDtIsNull(bookUser);
            boolean hasOverdueBooks = remainingHistories.stream().anyMatch(this::isOverdue);

            BookUser.StatusType status;
            if (hasOverdueBooks) {
                // 남은 대여 도서 중 연체가 있으면 overdue 상태
                status = BookUser.StatusType.overdue;
            } else if (remainingBorrowedCount >= 2) {
                // 연체는 없지만 2권 이상 대여 중이면 stop 상태
                status = BookUser.StatusType.stop;
            } else {
                // 연체도 없고 대여 권수도 2권 미만이면 available 상태
                status = BookUser.StatusType.available;
            }

            bookUserDAO.updateStatus(bookUser, status);
        }

        // 연체 반납인 경우 예외 발생
        if (isReturnedBookOverdue) {
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

    @Override
    public HistoryBookResponseDto getMyHistory(BookUser user) {
        List<RentalHistoryDto> rentalHistoryDtoList = historyDAO.getMyRentalHistoryList(user);
        if (rentalHistoryDtoList.isEmpty()) {
            throw new IllegalArgumentException("대여 기록이 없습니다.");
        }

        RentalSummaryDto rentalSummaryDto = historyDAO.getMyRentalSummay(user);
        return new HistoryBookResponseDto(rentalSummaryDto, rentalHistoryDtoList);
    }

    // 연체 인지
    private boolean isOverdue(History history) {
        return history.getReturnDt() == null && history.getBookDt().isBefore(LocalDate.now().minusDays(7));
    }

    @Override
    public List<PopularLabelDto> findPopularFirstSortByCourse(int courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findPopularFirstSortByCourse(courseId);
    }
    @Override
    public List<PopularLabelDto> findPopularFirstSortAll() {
        return historyDAO.findPopularFirstSortAll();
    }

    @Override
    public List<PopularLabelDto> findPopularSecondSortByCourse(int courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findPopularSecondSortByCourse(courseId);
    }

    @Override
    public List<PopularLabelDto> findPopularSecondSortAll() {
        return historyDAO.findPopularSecondSortAll();
    }

    @Override
    public List<UserReadingRankDto> findUserReadingRankByCourse(int courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findUserReadingRankByCourse(courseId);
    }

    @Override
    public List<UserReadingRankDto> findUserReadingRankAll() {
        return historyDAO.findUserReadingRankAll();
    }

}
