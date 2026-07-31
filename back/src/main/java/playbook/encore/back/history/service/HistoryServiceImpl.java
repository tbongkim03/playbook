package playbook.encore.back.history.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.admin.dao.AdminDAO;
import playbook.encore.back.book.dao.BookDAO;
import playbook.encore.back.bookUser.dao.BookUserDAO;
import playbook.encore.back.history.dao.HistoryDAO;
import playbook.encore.back.history.dto.*;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.book.entity.Book;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.history.entity.History;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.bookUser.dao.BookUserRepository;
import playbook.encore.back.book.dao.BookRepository;
import playbook.encore.back.history.dao.HistoryRepository;
import playbook.encore.back.favor.dao.FavorRepository;
import playbook.encore.back.course.dao.CourseRepository;
import playbook.encore.back.campus.dao.CampusRepository;
import playbook.encore.back.history.service.HistoryService;
import playbook.encore.back.discord.DiscordNotificationService;

import playbook.encore.back.common.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryDAO historyDAO;
    private final AdminRepository adminRepository;
    private final BookUserRepository bookUserRepository;
    private final BookRepository bookRepository;
    private final HistoryRepository historyRepository;
    private final FavorRepository favorRepository;
    private final AdminDAO adminDAO;
    private final BookUserDAO bookUserDAO;
    private final CourseRepository courseRepository;
    private final BookDAO bookDAO;
    private final DiscordNotificationService discordNotificationService;
    private final CampusRepository campusRepository;

    @Autowired
    public HistoryServiceImpl(HistoryDAO historyDAO, AdminRepository adminRepository, BookUserRepository bookUserRepository, BookRepository bookRepository, HistoryRepository historyRepository, FavorRepository favorRepository, AdminDAO adminDAO, BookUserDAO bookUserDAO, CourseRepository courseRepository, BookDAO bookDAO, DiscordNotificationService discordNotificationService, CampusRepository campusRepository) {
        this.historyDAO = historyDAO;
        this.adminRepository = adminRepository;
        this.bookUserRepository = bookUserRepository;
        this.bookRepository = bookRepository;
        this.historyRepository = historyRepository;
        this.favorRepository = favorRepository;
        this.adminDAO = adminDAO;
        this.bookUserDAO = bookUserDAO;
        this.courseRepository = courseRepository;
        this.bookDAO = bookDAO;
        this.discordNotificationService = discordNotificationService;
        this.campusRepository = campusRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleBookBorrow(Object human, String barcodeBook, Integer campusId) {
        log.info("[HistoryService] 도서 대출 처리 - barcode: {}, campusId: {}", barcodeBook, campusId);
        validateCampus(campusId);
        Object user = resolveUser(human);
        validateBorrowStatus(user);
        Book book = resolveBook(barcodeBook, campusId);
        validateBorrowEligibility(user, book);
        Campus campus = resolveCampus(campusId);
        saveBorrowHistory(user, book, campus);
        sendBorrowNotification(user, book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookReturnResultDto handleBookReturn(Object human, String barcodeBook, Integer campusId) {
        log.info("[HistoryService] 도서 반납 처리 - barcode: {}, campusId: {}", barcodeBook, campusId);
        validateCampus(campusId);
        Object user = resolveUser(human);
        Book book = resolveBook(barcodeBook, campusId);
        History history = resolveActiveHistory(user, book);
        boolean isReturnedBookOverdue = isOverdue(history);
        boolean isCourseFinished = checkCourseFinished(user);
        saveReturnHistory(user, book, history, isReturnedBookOverdue, isCourseFinished);
        sendReturnNotification(user, book);
        notifyFavorUsers(book);
        // 연체는 예외가 아니라 반환값으로 알린다. 여기서 예외를 던지면
        // rollbackFor = Exception.class에 걸려 위의 반납 처리가 전부 롤백된다.
        return resolveOverdueResult(user, history, isReturnedBookOverdue, isCourseFinished);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryBookResponseDto getHistoryBooks(Integer campusId) {
        log.info("[HistoryService] 대출/반납 내역 조회 - campusId: {}", campusId);
        RentalSummaryDto rentalSummaryDto;
        List<RentalHistoryDto> rentalHistoryDtoList;

        if (campusId == null) {
            rentalSummaryDto = historyDAO.getRentalSummay();
            rentalHistoryDtoList = historyDAO.getRentalHistoryList();
        } else {
            int totalRentals = historyRepository.countByBookDtIsNotNullAndSeqCampus_SeqCampus(campusId);
            int totalReturned = historyRepository.countByBookDtIsNotNullAndReturnDtIsNotNullAndSeqCampus_SeqCampus(campusId);
            int currentlyBorrowed = historyRepository.countByBookDtIsNotNullAndReturnDtIsNullAndSeqCampus_SeqCampus(campusId);
            int overdue = historyRepository.countByReturnDtIsNullAndBookDtBeforeAndSeqCampus_SeqCampus(LocalDate.now().minusDays(7), campusId);

            rentalSummaryDto = new RentalSummaryDto(totalRentals, totalReturned, currentlyBorrowed, overdue);
            rentalHistoryDtoList = historyDAO.getRentalHistoryListByCampus(campusId);
        }

        return new HistoryBookResponseDto(rentalSummaryDto, rentalHistoryDtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryBookResponseDto getHistoryBooks(Integer campusId, LocalDate startDate, LocalDate endDate) {
        log.info("[HistoryService] 날짜 범위 대출/반납 내역 조회 - campusId: {}, startDate: {}, endDate: {}", campusId, startDate, endDate);
        List<RentalHistoryDto> rentalHistoryDtoList;
        if (campusId == null) {
            rentalHistoryDtoList = historyDAO.getRentalHistoryListByDateRange(startDate, endDate);
        } else {
            rentalHistoryDtoList = historyDAO.getRentalHistoryListByCampusAndDateRange(campusId, startDate, endDate);
        }
        RentalSummaryDto summary = new RentalSummaryDto(rentalHistoryDtoList.size(), 0, 0, 0);
        return new HistoryBookResponseDto(summary, rentalHistoryDtoList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHistoryBook(int historyId) {
        log.info("[HistoryService] 대출 기록 삭제 - historyId: {}", historyId);
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대여 기록입니다."));
        historyDAO.deleteHistory(history);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryBookResponseDto getMyHistory(BookUser user) {
        log.info("[HistoryService] 내 대출 내역 조회 - userId: {}", user.getIdUser());
        List<RentalHistoryDto> rentalHistoryDtoList = historyDAO.getMyRentalHistoryList(user);
        if (rentalHistoryDtoList.isEmpty()) {
            throw new IllegalArgumentException("대여 기록이 없습니다.");
        }

        RentalSummaryDto rentalSummaryDto = historyDAO.getMyRentalSummay(user);
        return new HistoryBookResponseDto(rentalSummaryDto, rentalHistoryDtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportExcel(Integer campusId) throws IOException {
        log.info("[HistoryService] 대출이력 엑셀 내보내기 - campusId: {}", campusId);
        List<RentalHistoryDto> list = (campusId != null)
                ? historyDAO.getRentalHistoryListByCampus(campusId)
                : historyDAO.getRentalHistoryList();

        List<String> headers = Arrays.asList("도서명", "저자", "ISBN", "바코드", "이름", "아이디", "과정", "대출일", "반납일", "상태");
        List<List<Object>> rows = list.stream().map(h -> Arrays.<Object>asList(
                h.getBookTitle(),
                h.getBookAuthor(),
                h.getBookIsbn(),
                h.getBarcodeBook(),
                h.getUserName(),
                h.getUserId(),
                h.getCourseName() != null ? h.getCourseName() : "-",
                h.getBorrowDate() != null ? h.getBorrowDate().toString() : "-",
                h.getReturnDate() != null ? h.getReturnDate().toString() : "-",
                h.getStatus()
        )).collect(Collectors.toList());

        Workbook wb = ExcelUtil.createWorkbook(headers, rows);
        return ExcelUtil.toResponse(wb, "대출이력").getBody();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularLabelDto> findPopularFirstSortByCourse(int courseId, LocalDate startDate, LocalDate endDate) {
        log.info("[HistoryService] 과정별 대분류 인기 통계 - courseId: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findPopularFirstSortByCourse(courseId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularLabelDto> findPopularFirstSortAll(Integer campusId, LocalDate startDate, LocalDate endDate) {
        log.info("[HistoryService] 전체 대분류 인기 통계 - campusId: {}", campusId);
        if (campusId == null) {
            return historyDAO.findPopularFirstSortAll(startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return historyRepository.findPopularFirstSortAllByCampusAndDateRange(campusId, startDate, endDate);
        } else {
            return historyRepository.findPopularFirstSortAllByCampus(campusId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularLabelDto> findPopularSecondSortByCourse(int courseId, LocalDate startDate, LocalDate endDate) {
        log.info("[HistoryService] 과정별 소분류 인기 통계 - courseId: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findPopularSecondSortByCourse(courseId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularLabelDto> findPopularSecondSortAll(Integer campusId, LocalDate startDate, LocalDate endDate) {
        log.info("[HistoryService] 전체 소분류 인기 통계 - campusId: {}", campusId);
        if (campusId == null) {
            return historyDAO.findPopularSecondSortAll(startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return historyRepository.findPopularSecondSortAllByCampusAndDateRange(campusId, startDate, endDate);
        } else {
            return historyRepository.findPopularSecondSortAllByCampus(campusId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReadingRankDto> findUserReadingRankByCourse(int courseId, LocalDate startDate, LocalDate endDate) {
        log.info("[HistoryService] 과정별 독서량 순위 - courseId: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findUserReadingRankByCourse(courseId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReadingRankDto> findUserReadingRankAll(Integer campusId, LocalDate startDate, LocalDate endDate) {
        log.info("[HistoryService] 전체 독서량 순위 - campusId: {}", campusId);
        if (campusId == null) {
            return historyDAO.findUserReadingRankAll(startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return historyRepository.findUserReadingRankAllByCampusAndDateRange(campusId, startDate, endDate);
        } else {
            return historyRepository.findUserReadingRankAllByCampus(campusId);
        }
    }

    // ─── 공통 헬퍼 ───────────────────────────────────────────────────────────

    private void validateCampus(Integer campusId) {
        if (campusId == null) {
            throw new IllegalArgumentException("캠퍼스 정보가 필요합니다.");
        }
    }

    private Object resolveUser(Object human) {
        if (human instanceof BookUser bookUser) {
            return bookUserRepository.findByIdUser(bookUser.getIdUser())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        } else if (human instanceof Admin admin) {
            return adminRepository.findByIdAdmin(admin.getIdAdmin())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운영자입니다."));
        }
        throw new IllegalArgumentException("잘못된 사용자 타입입니다.");
    }

    private Book resolveBook(String barcode, Integer campusId) {
        return bookRepository.findByBarcodeBookAndSeqCampus_SeqCampus(barcode, campusId)
                .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스에 존재하지 않는 도서입니다."));
    }

    private boolean isOverdue(History history) {
        return history.getReturnDt() == null && history.getBookDt().isBefore(LocalDate.now().minusDays(7));
    }

    // ─── 대출 전용 헬퍼 ───────────────────────────────────────────────────────

    private void validateBorrowStatus(Object user) {
        if (user instanceof Admin admin) {
            Admin.StatusTypeAdmin status = admin.getStatusAdmin();
            if (status == Admin.StatusTypeAdmin.overdue || status == Admin.StatusTypeAdmin.stop) {
                throw new IllegalArgumentException("대여 불가능한 상태입니다. 현재 상태: " + status);
            }
        } else if (user instanceof BookUser bookUser) {
            BookUser.StatusType status = bookUser.getStatusUser();
            if (status == BookUser.StatusType.overdue || status == BookUser.StatusType.stop) {
                throw new IllegalArgumentException("대여 불가능한 상태입니다. 현재 상태: " + status);
            }
        }
    }

    private void validateBorrowEligibility(Object user, Book book) {
        if (historyRepository.existsBySeqBookAndReturnDtIsNull(book)) {
            throw new IllegalArgumentException("이미 대여중인 도서입니다.");
        }

        if (user instanceof Admin admin) {
            if (historyRepository.existsBySeqBookAndSeqAdminAndReturnDtIsNull(book, admin)) {
                throw new IllegalArgumentException("이미 대여중인 도서입니다.");
            }
        } else if (user instanceof BookUser bookUser) {
            List<History> unreturnedHistories = historyRepository.findAllBySeqUserAndReturnDtIsNull(bookUser);
            if (unreturnedHistories.stream().anyMatch(this::isOverdue)) {
                throw new IllegalArgumentException("연체 중인 도서가 있어 대출할 수 없습니다.");
            }
            if (historyRepository.countBySeqUserAndReturnDtIsNull(bookUser) >= 2) {
                throw new IllegalArgumentException("최대 2권까지 대여할 수 있습니다.");
            }
            if (historyRepository.existsBySeqBookAndSeqUserAndReturnDtIsNull(book, bookUser)) {
                throw new IllegalArgumentException("이미 대여중인 도서입니다.");
            }
        }
    }

    private Campus resolveCampus(Integer campusId) {
        return campusRepository.findById(campusId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 캠퍼스입니다."));
    }

    private void saveBorrowHistory(Object user, Book book, Campus campus) {
        History.HistoryBuilder historyBuilder = History.builder()
                .seqBook(book)
                .seqCampus(campus)
                .bookDt(LocalDate.now())
                .returnDt(null);

        if (user instanceof Admin admin) {
            historyBuilder.seqAdmin(admin).seqUser(null).seqCourse(null);
        } else if (user instanceof BookUser bookUser) {
            historyBuilder.seqUser(bookUser).seqCourse(bookUser.getSeqCourse()).seqAdmin(null);
        }
        historyDAO.bookBorrow(historyBuilder.build());

        try {
            bookDAO.bookStatusUpdate(book, true);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (user instanceof Admin admin) {
            adminDAO.updateStatus(admin, Admin.StatusTypeAdmin.available);
        } else if (user instanceof BookUser bookUser) {
            int count = historyRepository.countBySeqUserAndReturnDtIsNull(bookUser);
            BookUser.StatusType status = count >= 2 ? BookUser.StatusType.stop : BookUser.StatusType.available;
            bookUserDAO.updateStatus(bookUser, status);
        }
    }

    private void sendBorrowNotification(Object user, Book book) {
        String discordId = user instanceof BookUser bu ? bu.getDcUser() : ((Admin) user).getDcAdmin();
        String userName = user instanceof BookUser bu ? bu.getNameUser() : ((Admin) user).getNameAdmin();
        try {
            discordNotificationService.sendBorrowNotification(
                    discordId, userName, book.getTitleBook(),
                    LocalDate.now().plusDays(7).toString());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // ─── 반납 전용 헬퍼 ───────────────────────────────────────────────────────

    private History resolveActiveHistory(Object user, Book book) {
        if (user instanceof Admin admin) {
            return historyRepository.findBySeqBookAndSeqAdminAndReturnDtIsNull(book, admin)
                    .orElseThrow(() -> new IllegalArgumentException("반납할 대여 기록이 없습니다."));
        }
        BookUser bookUser = (BookUser) user;
        return historyRepository.findBySeqBookAndSeqUserAndReturnDtIsNull(book, bookUser)
                .orElseThrow(() -> new IllegalArgumentException("반납할 대여 기록이 없습니다."));
    }

    private boolean checkCourseFinished(Object user) {
        if (user instanceof BookUser bookUser) {
            return bookUser.getSeqCourse().getFinishDtCourse().isBefore(LocalDate.now());
        }
        return false;
    }

    private void saveReturnHistory(Object user, Book book, History history, boolean isReturnedBookOverdue, boolean isCourseFinished) {
        history.setReturnDt(LocalDate.now());
        historyDAO.bookReturn(history);

        try {
            bookDAO.bookStatusUpdate(book, false);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (user instanceof Admin admin) {
            if (isReturnedBookOverdue) {
                adminDAO.updateStatus(admin, Admin.StatusTypeAdmin.stop);
            } else {
                List<History> remaining = historyRepository.findAllBySeqAdminAndReturnDtIsNull(admin);
                Admin.StatusTypeAdmin status = remaining.stream().anyMatch(this::isOverdue)
                        ? Admin.StatusTypeAdmin.overdue : Admin.StatusTypeAdmin.available;
                adminDAO.updateStatus(admin, status);
            }
        } else if (user instanceof BookUser bookUser) {
            if (isCourseFinished) {
                bookUserDAO.updateStatus(bookUser, BookUser.StatusType.stop);
            } else {
                int remaining = historyRepository.countBySeqUserAndReturnDtIsNull(bookUser);
                List<History> remainingHistories = historyRepository.findAllBySeqUserAndReturnDtIsNull(bookUser);
                boolean hasOverdue = remainingHistories.stream().anyMatch(this::isOverdue);

                BookUser.StatusType status;
                if (isReturnedBookOverdue) {
                    status = BookUser.StatusType.stop;
                } else if (hasOverdue) {
                    status = BookUser.StatusType.overdue;
                } else if (remaining >= 2) {
                    status = BookUser.StatusType.stop;
                } else {
                    status = BookUser.StatusType.available;
                }
                bookUserDAO.updateStatus(bookUser, status);
            }
        }
    }

    private void sendReturnNotification(Object user, Book book) {
        String discordId = user instanceof BookUser bu ? bu.getDcUser() : ((Admin) user).getDcAdmin();
        String userName = user instanceof BookUser bu ? bu.getNameUser() : ((Admin) user).getNameAdmin();
        try {
            discordNotificationService.sendReturnNotification(discordId, userName, book.getTitleBook());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void notifyFavorUsers(Book book) {
        List<BookUser> favorUsers = favorRepository.findAllBySeqBook(book);
        for (BookUser favorUser : favorUsers) {
            try {
                discordNotificationService.sendFavorNotification(
                        favorUser.getDcUser(), favorUser.getNameUser(), book.getTitleBook());
            } catch (Exception e) {
                System.err.println("즐겨찾기 알림 전송 실패: " + e.getMessage());
            }
        }
    }

    /**
     * 연체 안내 대상인지 판정해 결과로 돌려준다.
     *
     * <p>판정 조건은 기존 {@code checkOverdueException()}과 동일하다 —
     * 연체 반납이면서 (관리자이거나 과정이 아직 종료되지 않은 경우).
     * 과정이 종료된 일반 사용자는 {@code saveReturnHistory()}에서 이미 {@code stop} 처리되므로
     * 연체 안내를 하지 않는다.</p>
     */
    private BookReturnResultDto resolveOverdueResult(Object user, History history, boolean isReturnedBookOverdue, boolean isCourseFinished) {
        if (!isReturnedBookOverdue) return BookReturnResultDto.normal();
        boolean isAdmin = user instanceof Admin;
        if (!isAdmin && isCourseFinished) return BookReturnResultDto.normal();
        LocalDate dueDate = history.getBookDt().plusDays(7);
        long overdueDays = LocalDate.now().toEpochDay() - dueDate.toEpochDay();
        return BookReturnResultDto.overdue(overdueDays);
    }
}
