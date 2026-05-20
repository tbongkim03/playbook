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
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.book.entity.Book;
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

import java.time.LocalDate;
import java.util.List;

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
        // 캠퍼스 검증
        if (campusId == null) {
            throw new IllegalArgumentException("캠퍼스 정보가 필요합니다.");
        }
        Object user;
        String userName;
        String discordId;
        boolean isAdmin = false;

        if (human instanceof BookUser bookUser) {
            user = bookUserRepository.findByIdUser(bookUser.getIdUser())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            userName = ((BookUser) user).getNameUser();
            discordId = ((BookUser) user).getDcUser();
        } else if (human instanceof Admin admin) {
            user = adminRepository.findByIdAdmin(admin.getIdAdmin())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운영자입니다."));
            isAdmin = true;
            userName = ((Admin) user).getNameAdmin();
            discordId = ((Admin) user).getDcAdmin();
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

        // 도서 조회 시 캠퍼스 필터
        Book book = bookRepository.findByBarcodeBookAndSeqCampus_SeqCampus(barcodeBook, campusId)
                .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스에 존재하지 않는 도서입니다."));

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

        // Campus 엔티티 조회
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 캠퍼스입니다."));

        // 대여 기록 생성
        History.HistoryBuilder historyBuilder = History.builder()
                .seqBook(book)
                .seqCampus(campus)  // 캠퍼스 설정
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
            discordNotificationService.sendBorrowNotification(
                    discordId,
                    userName,
                    borrowBook.getTitleBook(),
                    LocalDate.now().plusDays(7).toString()
            );
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
    @Transactional(rollbackFor = Exception.class)
    public void handleBookReturn(Object human, String barcodeBook, Integer campusId) {
        log.info("[HistoryService] 도서 반납 처리 - barcode: {}, campusId: {}", barcodeBook, campusId);
        // 캠퍼스 검증
        if (campusId == null) {
            throw new IllegalArgumentException("캠퍼스 정보가 필요합니다.");
        }
        Object user;
        boolean isAdmin = false;
        String userName;
        String discordId;

        if (human instanceof BookUser bookUser) {
            user = bookUserRepository.findByIdUser(bookUser.getIdUser())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            userName = ((BookUser) user).getNameUser();
            discordId = ((BookUser) user).getDcUser();
        } else if (human instanceof Admin admin) {
            user = adminRepository.findByIdAdmin(admin.getIdAdmin())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운영자입니다."));
            userName = ((Admin) user).getNameAdmin();
            discordId = ((Admin) user).getDcAdmin();
            isAdmin = true;
        } else {
            throw new IllegalArgumentException("잘못된 사용자 타입입니다.");
        }

        // 도서 조회 시 캠퍼스 필터
        Book book = bookRepository.findByBarcodeBookAndSeqCampus_SeqCampus(barcodeBook, campusId)
                .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스에 존재하지 않는 도서입니다."));

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

        // 반납일 설정 전에 연체 여부 확인
        boolean isReturnedBookOverdue = isOverdue(history);

        LocalDate today = LocalDate.now();
        
        // 과정 종료 여부 확인 (BookUser인 경우에만)
        boolean isCourseFinished = false;
        if (!isAdmin) {
            BookUser bookUser = (BookUser) user;
            isCourseFinished = bookUser.getSeqCourse().getFinishDtCourse().isBefore(today);
        }
        history.setReturnDt(today);
        historyDAO.bookReturn(history);

        try {
            // 반납 기록 저장 후 상태 업데이트
            Book borrowBook = bookDAO.bookStatusUpdate(book, false);
            discordNotificationService.sendReturnNotification(
                    discordId,
                    userName,
                    borrowBook.getTitleBook()
            );
        } catch (Exception e) {
            // 반납 기록 저장 실패 시 롤백
            throw new RuntimeException(e.getMessage());
        }

        // 상태 업데이트
        if (isAdmin) {
            Admin admin = (Admin) user;

            if (isReturnedBookOverdue) {
                // 연체 반납 시 정지 상태로 변경
                adminDAO.updateStatus(admin, Admin.StatusTypeAdmin.stop);
            } else {
                // 남은 대여 도서 중 연체가 있는지 확인
                List<History> remainingHistories = historyRepository.findAllBySeqAdminAndReturnDtIsNull(admin);
                boolean hasOverdueBooks = remainingHistories.stream().anyMatch(this::isOverdue);

                Admin.StatusTypeAdmin status = hasOverdueBooks ?
                        Admin.StatusTypeAdmin.overdue : Admin.StatusTypeAdmin.available;
                adminDAO.updateStatus(admin, status);
            }
        } else {
            BookUser bookUser = (BookUser) user;

            // 과정이 종료된 경우 stop 상태 유지
            if (isCourseFinished) {
                bookUserDAO.updateStatus(bookUser, BookUser.StatusType.stop);
            } else {
                // 반납 후 현재 대여 중인 도서 수 확인
                int remainingBorrowedCount = historyRepository.countBySeqUserAndReturnDtIsNull(bookUser);

                // 나머지 대여 중인 도서들 중 연체된 것이 있는지 확인
                List<History> remainingHistories = historyRepository.findAllBySeqUserAndReturnDtIsNull(bookUser);
                boolean hasOverdueBooks = remainingHistories.stream().anyMatch(this::isOverdue);

                BookUser.StatusType status;

                if (isReturnedBookOverdue) {
                    // 연체 반납인 경우 정지 상태
                    status = BookUser.StatusType.stop;
                } else if (hasOverdueBooks) {
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
        }

        // 즐겨찾기 알림
        List<BookUser> favorUsers = favorRepository.findAllBySeqBook(book);
        for (BookUser favorUser : favorUsers) {
            try {
                discordNotificationService.sendFavorNotification(
                        favorUser.getDcUser(),
                        favorUser.getNameUser(),
                        book.getTitleBook()
                );
            } catch (Exception e) {
                System.err.println("즐겨찾기 알림 전송 실패: " + e.getMessage());
            }
        }

        // 연체 반납인 경우 예외 발생 (단, 과정이 종료된 학생의 경우 예외 발생하지 않음)
        if (isReturnedBookOverdue) {
            if (isAdmin) {
                // Admin의 경우 연체 반납 시 예외 발생
                LocalDate dueDate = history.getBookDt().plusDays(7);
                long overdueDays = today.toEpochDay() - dueDate.toEpochDay();
                throw new IllegalArgumentException("연체 반납되었습니다. 연체일수: " + overdueDays + "일");
            } else if (!isCourseFinished) {
                // 과정이 종료되지 않은 학생의 경우에만 예외 발생
                LocalDate dueDate = history.getBookDt().plusDays(7);
                long overdueDays = today.toEpochDay() - dueDate.toEpochDay();
                throw new IllegalArgumentException("연체 반납되었습니다. 연체일수: " + overdueDays + "일");
            }
            // 과정이 종료된 학생의 경우 예외 발생하지 않음 (stop 상태 유지)
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryBookResponseDto getHistoryBooks(Integer campusId) {
        log.info("[HistoryService] 대출/반납 내역 조회 - campusId: {}", campusId);
        RentalSummaryDto rentalSummaryDto;
        List<RentalHistoryDto> rentalHistoryDtoList;

        if (campusId == null) {
            // 전체 관리자: 모든 캠퍼스 히스토리
            rentalSummaryDto = historyDAO.getRentalSummay();
            rentalHistoryDtoList = historyDAO.getRentalHistoryList();
        } else {
            // 특정 캠퍼스 관리자: 해당 캠퍼스만
            // 캠퍼스별 통계를 직접 계산
            int totalRentals = historyRepository.countByBookDtIsNotNullAndSeqCampus_SeqCampus(campusId);
            int totalReturned = historyRepository.countByBookDtIsNotNullAndReturnDtIsNotNullAndSeqCampus_SeqCampus(campusId);
            int currentlyBorrowed = historyRepository.countByBookDtIsNotNullAndReturnDtIsNullAndSeqCampus_SeqCampus(campusId);
            int overdue = historyRepository.countByReturnDtIsNullAndBookDtBeforeAndSeqCampus_SeqCampus(LocalDate.now().minusDays(7), campusId);

            rentalSummaryDto = new RentalSummaryDto(totalRentals, totalReturned, currentlyBorrowed, overdue);

            // 캠퍼스별 대여 내역 조회
            rentalHistoryDtoList = historyDAO.getRentalHistoryListByCampus(campusId);
        }

        return new HistoryBookResponseDto(rentalSummaryDto, rentalHistoryDtoList);
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

    // 연체 판단 메서드
    private boolean isOverdue(History history) {
        return history.getReturnDt() == null && history.getBookDt().isBefore(LocalDate.now().minusDays(7));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularLabelDto> findPopularFirstSortByCourse(int courseId) {
        log.info("[HistoryService] 과정별 대분류 인기 통계 - courseId: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findPopularFirstSortByCourse(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularLabelDto> findPopularFirstSortAll(Integer campusId) {
        log.info("[HistoryService] 전체 대분류 인기 통계 - campusId: {}", campusId);
        if (campusId == null) {
            // 전체 관리자: 모든 캠퍼스 통계
            return historyDAO.findPopularFirstSortAll();
        } else {
            // 특정 캠퍼스 관리자: 해당 캠퍼스만
            return historyRepository.findPopularFirstSortAllByCampus(campusId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularLabelDto> findPopularSecondSortByCourse(int courseId) {
        log.info("[HistoryService] 과정별 소분류 인기 통계 - courseId: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findPopularSecondSortByCourse(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularLabelDto> findPopularSecondSortAll(Integer campusId) {
        log.info("[HistoryService] 전체 소분류 인기 통계 - campusId: {}", campusId);
        if (campusId == null) {
            // 전체 관리자: 모든 캠퍼스 통계
            return historyDAO.findPopularSecondSortAll();
        } else {
            // 특정 캠퍼스 관리자: 해당 캠퍼스만
            return historyRepository.findPopularSecondSortAllByCampus(campusId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReadingRankDto> findUserReadingRankByCourse(int courseId) {
        log.info("[HistoryService] 과정별 독서량 순위 - courseId: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("유효하지 않은 과정 ID입니다.");
        }
        return historyDAO.findUserReadingRankByCourse(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReadingRankDto> findUserReadingRankAll(Integer campusId) {
        log.info("[HistoryService] 전체 독서량 순위 - campusId: {}", campusId);
        if (campusId == null) {
            // 전체 관리자: 모든 캠퍼스 통계
            return historyDAO.findUserReadingRankAll();
        } else {
            // 특정 캠퍼스 관리자: 해당 캠퍼스만
            return historyRepository.findUserReadingRankAllByCampus(campusId);
        }
    }
}