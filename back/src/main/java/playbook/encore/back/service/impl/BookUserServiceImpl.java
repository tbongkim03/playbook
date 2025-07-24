package playbook.encore.back.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.data.dto.bookUser.RegisterIdValidateResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserRequestDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.Course;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.data.repository.CourseRepository;
import playbook.encore.back.service.BookUserService;

@Service
public class BookUserServiceImpl implements BookUserService{

    private final BookUserDAO bookUserDAO;
    private final BookUserRepository bookUserRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public BookUserServiceImpl(BookUserDAO bookUserDAO, BookUserRepository bookUserRepository, CourseRepository courseRepository) {
        this.bookUserDAO = bookUserDAO;
        this.bookUserRepository = bookUserRepository;
        this.courseRepository = courseRepository;
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
        boolean flag = bookUserDAO.searchBookUserResultExact(idUser).isPresent();
        return new RegisterIdValidateResponseDto(flag);
    }
    
}
