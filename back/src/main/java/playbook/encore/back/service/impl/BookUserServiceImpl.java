package playbook.encore.back.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import playbook.encore.back.data.dao.AdminDAO;
import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.data.dto.bookUser.LoginUserRequestDto;
import playbook.encore.back.data.dto.bookUser.LoginUserResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterIdValidateResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserRequestDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.Course;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.data.repository.CourseRepository;
import playbook.encore.back.jwt.jwtUtil;
import playbook.encore.back.service.BookUserService;

@Service
public class BookUserServiceImpl implements BookUserService{

    private final AdminDAO adminDAO;
    private final BookUserDAO bookUserDAO;
    private final CourseRepository courseRepository;
    private final jwtUtil jwtUtil;

    @Autowired
    public BookUserServiceImpl(AdminDAO adminDAO, BookUserDAO bookUserDAO, CourseRepository courseRepository, jwtUtil jwtUtil) {
        this.adminDAO = adminDAO;
        this.bookUserDAO = bookUserDAO;
        this.courseRepository = courseRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public RegisterUserResponseDto createUser(RegisterUserRequestDto registerUserRequestDto) {
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
                .build();
        
        BookUser savedBookUser = bookUserDAO.createUser(bookUser);

        // httpstatus, code, message, data 필요
        return new RegisterUserResponseDto(200, HttpStatus.OK, "회원가입을 완료하였습니다", null);
    }

    @Override
    public RegisterIdValidateResponseDto checkUserId(String idUser) {
        boolean flag = bookUserDAO.searchBookUserResultExact(idUser).isPresent() || adminDAO.searchBookUserResultExact(idUser).isPresent();
        return new RegisterIdValidateResponseDto(flag);
    }

    @Override
    public LoginUserResponseDto loginServiceUser(LoginUserRequestDto loginUserRequestDto) {
        String id = loginUserRequestDto.getIdUser();
        String pw = loginUserRequestDto.getPwUser();
        boolean isLoginSuccess = bookUserDAO.loginIdPwCheck(id, pw).isPresent();
        if (!isLoginSuccess) {
            throw new IllegalArgumentException("로그인에 실패하였습니다. 아이디와 비밀번호를 확인해 주세요.");
        } 
        return new LoginUserResponseDto(jwtUtil.generateToken(id));
    }

    @Override
    public boolean validatePassword(BookUser bookUser, String password) {
        boolean isPasswordExist = bookUserDAO.pwValidate(bookUser, password).isPresent();
        if (!isPasswordExist) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다. 다시 입력해 주세요");
        }
        return true;
    }

    @Override
    public boolean updatePassword(BookUser bookUser, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        boolean isPasswordChanged = bookUserDAO.changePw(bookUser, hashedPassword).isPresent();
        if (!isPasswordChanged) {
            throw new IllegalArgumentException("비밀번호 변경에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }

    @Override
    public boolean updateDiscord(BookUser bookUser, String newDiscord) {
        boolean isDiscordChanged = bookUserDAO.changeDiscord(bookUser, newDiscord).isPresent();
        if (!isDiscordChanged) {
            throw new IllegalArgumentException("과정 변경에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }

    @Override
    public boolean updateCourse(BookUser bookUser, Integer newSeqCourse) {
        boolean isCourseChanged = bookUserDAO.changeCourse(bookUser, newSeqCourse).isPresent();
        if (!isCourseChanged) {
            throw new IllegalArgumentException("과정 변경에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }



}
