package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.HistoryDAO;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.History;
import playbook.encore.back.data.repository.*;
import playbook.encore.back.service.HistoryService;

import java.time.LocalDate;

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

        if (isAdmin) {
            Admin admin = (Admin) user;
            boolean alreadyBorrowed = historyRepository.existsBySeqBookAndSeqAdminAndReturnDtIsNull(book, admin);
            if (alreadyBorrowed) {
                throw new IllegalArgumentException("이미 대여중인 도서입니다.");
            }
        } else {
            BookUser bookUser = (BookUser) user;
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
    }

}
