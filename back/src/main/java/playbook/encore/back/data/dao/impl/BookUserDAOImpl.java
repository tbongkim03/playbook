package playbook.encore.back.data.dao.impl;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.data.dto.bookUser.LoginUserResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.Course;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.data.repository.CourseRepository;

@Component
@RequiredArgsConstructor
public class BookUserDAOImpl implements BookUserDAO {

    private final BookUserRepository bookUserRepository;
    private final CourseRepository courseRepository;

    @Override
    public BookUser createUser(BookUser bookUser) {
        return bookUserRepository.save(bookUser);
    }

    @Override
    public Optional<BookUser> searchBookUserResultExact(String idUser) {
        return bookUserRepository.findByIdUser(idUser);
    }

    @Override
    public Optional<Boolean> loginIdPwCheck(String idUser, String pwUser) {
        Optional<BookUser> optionalUser = bookUserRepository.findByIdUser(idUser);
        if (optionalUser.isEmpty()) return Optional.empty();
        BookUser user = optionalUser.get();
        return Optional.of(BCrypt.checkpw(pwUser, user.getPwUser()));
    }

    @Override
    public Optional<BookUser> pwValidate(BookUser bookUser, String password) {
        Optional<BookUser> optionalUser = bookUserRepository.findByIdUser(bookUser.getIdUser());
        if (optionalUser.isEmpty()) return Optional.empty();
        BookUser user = optionalUser.get();
        if (BCrypt.checkpw(password, user.getPwUser())) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<BookUser> changePw(BookUser bookUser, String hashedPassword) {
        Optional<BookUser> optionalUser = bookUserRepository.findByIdUser(bookUser.getIdUser());
        if (optionalUser.isEmpty()) return Optional.empty();
        BookUser selectedUser = optionalUser.get();

        BookUser updatedUser;
        selectedUser.setPwUser(hashedPassword);
        updatedUser = bookUserRepository.save(selectedUser);

        return Optional.of(updatedUser);

    }

    @Override
    public Optional<BookUser> changeCourse(BookUser bookUser, Integer newSeqCourse) {
        Optional<BookUser> optionalUser = bookUserRepository.findByIdUser(bookUser.getIdUser());
        if (optionalUser.isEmpty()) return Optional.empty();
        Optional<Course> optionalCourse = courseRepository.findById(newSeqCourse);
        if (optionalCourse.isEmpty()) return Optional.empty();
        BookUser selectedUser = optionalUser.get();
        Course selectedCourse = optionalCourse.get();
        BookUser updatedUser;

        selectedUser.setSeqCourse(selectedCourse);
        updatedUser = bookUserRepository.save(selectedUser);

        return Optional.of(updatedUser);
    }

    @Override
    public Optional<BookUser> changeDiscord(BookUser bookUser, String newDiscord) {
        Optional<BookUser> optionalUser = bookUserRepository.findByIdUser(bookUser.getIdUser());
        if (optionalUser.isEmpty()) return Optional.empty();
        BookUser selectedUser = optionalUser.get();
        BookUser updatedUser;

        selectedUser.setDcUser(newDiscord);
        updatedUser = bookUserRepository.save(selectedUser);

        return Optional.of(updatedUser);
    }

}
