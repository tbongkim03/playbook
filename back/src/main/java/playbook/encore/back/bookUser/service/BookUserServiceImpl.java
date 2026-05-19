package playbook.encore.back.bookUser.service;

import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.admin.dao.AdminDAO;
import playbook.encore.back.bookUser.dao.BookUserDAO;
import playbook.encore.back.bookUser.dto.LoginUserRequestDto;
import playbook.encore.back.bookUser.dto.RegisterIdValidateResponseDto;
import playbook.encore.back.bookUser.dto.RegisterUserRequestDto;
import playbook.encore.back.bookUser.dto.RegisterUserResponseDto;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.course.entity.Course;
import playbook.encore.back.bookUser.dao.BookUserRepository;
import playbook.encore.back.course.dao.CourseRepository;
import playbook.encore.back.history.dao.HistoryRepository;
import playbook.encore.back.history.entity.History;
import playbook.encore.back.bookUser.service.BookUserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BookUserServiceImpl implements BookUserService{

    private final AdminDAO adminDAO;
    private final BookUserDAO bookUserDAO;
    private final CourseRepository courseRepository;
    private final BookUserRepository bookUserRepository;
    private final HistoryRepository historyRepository;

    @Autowired
    public BookUserServiceImpl(AdminDAO adminDAO, BookUserDAO bookUserDAO, CourseRepository courseRepository, BookUserRepository bookUserRepository, HistoryRepository historyRepository) {
        this.adminDAO = adminDAO;
        this.bookUserDAO = bookUserDAO;
        this.courseRepository = courseRepository;
        this.bookUserRepository = bookUserRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public RegisterUserResponseDto createUser(RegisterUserRequestDto registerUserRequestDto) {
        log.info("[BookUserService] 사용자 등록 - id: {}", registerUserRequestDto.getIdUser());
        Course course = courseRepository.findById(registerUserRequestDto.getSeqCorse())
        .orElseThrow(() -> new IllegalArgumentException("해당 과정은 존재하지 않습니다."));

        String hashedPassword = BCrypt.hashpw(registerUserRequestDto.getPwUser(), BCrypt.gensalt());

        BookUser bookUser = BookUser.builder()
                .seqCourse(course)
                .idUser(registerUserRequestDto.getIdUser())
                .pwUser(hashedPassword)
                .nameUser(registerUserRequestDto.getNameUser())
                .dcUser(registerUserRequestDto.getDcUser())
                .agreeTermsUser(registerUserRequestDto.isAgreeTermsUser())
                .agreeInfoUser(registerUserRequestDto.isAgreeTermsUser())
                .agreeDiscordAlarmUser(registerUserRequestDto.isAgreeDiscordAlarmUser())
                .statusUser(BookUser.StatusType.available)
                .createdAt(LocalDate.now())
                .build();
        
        bookUserDAO.createUser(bookUser);

        // httpstatus, code, message, data 필요
        return new RegisterUserResponseDto(200, HttpStatus.OK, "회원가입을 완료하였습니다", null);
    }

    @Override
    public RegisterIdValidateResponseDto checkUserId(String idUser) {
        log.info("[BookUserService] 아이디 중복 확인 - id: {}", idUser);
        boolean flag = bookUserDAO.searchBookUserResultExact(idUser).isPresent() || adminDAO.searchBookUserResultExact(idUser).isPresent();
        return new RegisterIdValidateResponseDto(flag);
    }

    @Override
    public String loginServiceUser(LoginUserRequestDto loginUserRequestDto) {
        log.info("[BookUserService] 사용자 로그인 시도 - id: {}", loginUserRequestDto.getIdUser());
        String id = loginUserRequestDto.getIdUser();
        String pw = loginUserRequestDto.getPwUser();
        boolean isLoginSuccess = bookUserDAO.loginIdPwCheck(id, pw).isPresent();
        if (!isLoginSuccess) {
            throw new IllegalArgumentException("로그인에 실패하였습니다. 아이디와 비밀번호를 확인해 주세요.");
        }
        return id;
    }

    @Override
    public boolean validatePassword(BookUser bookUser, String password) {
        log.info("[BookUserService] 비밀번호 검증 - id: {}", bookUser.getIdUser());
        boolean isPasswordExist = bookUserDAO.pwValidate(bookUser, password).isPresent();
        if (!isPasswordExist) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다. 다시 입력해 주세요");
        }
        return true;
    }

    @Override
    public boolean updatePassword(BookUser bookUser, String newPassword) {
        log.info("[BookUserService] 비밀번호 변경 - id: {}", bookUser.getIdUser());
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호가 비어있습니다.");
        }
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        boolean isPasswordChanged = bookUserDAO.changePw(bookUser, hashedPassword).isPresent();
        if (!isPasswordChanged) {
            throw new IllegalArgumentException("비밀번호 변경에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }

//    @Override
//    public boolean updateDiscord(BookUser bookUser, String newDiscord) {
//        boolean isDiscordChanged = bookUserDAO.changeDiscord(bookUser, newDiscord).isPresent();
//        if (!isDiscordChanged) {
//            throw new IllegalArgumentException("과정 변경에 실패하였습니다. 다시 시도해 주세요");
//        }
//        return true;
//    }

//    @Override
//    public boolean updateCourse(BookUser bookUser, Integer newSeqCourse) {
//        boolean isCourseChanged = bookUserDAO.changeCourse(bookUser, newSeqCourse).isPresent();
//        if (!isCourseChanged) {
//            throw new IllegalArgumentException("과정 변경에 실패하였습니다. 다시 시도해 주세요");
//        }
//        return true;
//    }

    @Override
    public List<Object[]> getBookUserList(Integer campusId) {
        log.info("[BookUserService] 사용자 목록 조회 - campusId: {}", campusId);
        if (campusId == null) {
            // 전체 관리자: 모든 캠퍼스 사용자
            return bookUserDAO.getBookUserList();
        } else {
            // 특정 캠퍼스 관리자: 해당 캠퍼스 사용자만
            return bookUserRepository.findAllUsersWithCourseDetailsByCampus(campusId);
        }
    }

    @Override
    @Transactional
    public boolean deleteUserByAdmin(String idUser) {
        log.info("[BookUserService] 관리자에 의한 사용자 삭제 - id: {}", idUser);
        try {
            Optional<BookUser> optionalUser = bookUserDAO.searchBookUserResultExact(idUser);
            if (optionalUser.isEmpty()) {
                throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
            }

            BookUser user = optionalUser.get();

            // 현재 대출 중인 책 확인
            boolean hasActiveBorrows = historyRepository.existsBySeqUserAndReturnDtIsNull(user);
            if (hasActiveBorrows) {
                throw new IllegalArgumentException("대출 중인 책이 있어 삭제할 수 없습니다.");
            }

            // 찜 기록 삭제
//            favorRepository.deleteBySeqUser(user);
//            favorRepository.flush();

            bookUserRepository.deleteById(user.getSeqUser());
            bookUserRepository.flush();

            return true;
        } catch (Exception e) {
            throw new IllegalArgumentException("유저 삭제에 실패하였습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteUserBySelf(BookUser user) {
        log.info("[BookUserService] 사용자 탈퇴 - id: {}", user.getIdUser());
        try {
            // 현재 대출 중인 책 확인
            List<playbook.encore.back.history.entity.History> unreturnedHistories = 
                historyRepository.findAllBySeqUserAndReturnDtIsNull(user);
            
            if (!unreturnedHistories.isEmpty()) {
                // 연체 중인 도서가 있는지 확인 (HistoryServiceImpl의 isOverdue 로직과 동일)
                boolean hasOverdue = unreturnedHistories.stream().anyMatch(history -> {
                    return history.getReturnDt() == null && 
                           history.getBookDt().isBefore(LocalDate.now().minusDays(7));
                });
                
                if (hasOverdue) {
                    throw new IllegalArgumentException("연체 중인 도서가 있어 탈퇴할 수 없습니다. 먼저 연체된 도서를 반납해주세요.");
                }
                
                throw new IllegalArgumentException("대출 중인 도서가 있어 탈퇴할 수 없습니다. 먼저 모든 도서를 반납해주세요.");
            }

            // 사용자 삭제 (찜 목록은 외래키 CASCADE로 자동 삭제됨)
            bookUserRepository.deleteById(user.getSeqUser());
            bookUserRepository.flush();

            return true;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("회원 탈퇴에 실패하였습니다: " + e.getMessage());
        }
    }
}
