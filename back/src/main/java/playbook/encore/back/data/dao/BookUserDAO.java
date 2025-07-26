package playbook.encore.back.data.dao;

import java.util.Optional;

import playbook.encore.back.data.dto.bookUser.LoginUserResponseDto;
import playbook.encore.back.data.entity.BookUser;

public interface BookUserDAO {

    BookUser createUser(BookUser bookUser);

    Optional<BookUser> searchBookUserResultExact(String idUser);

    boolean loginIdPwCheck(String id, String pw);

    Optional<BookUser> pwValidate(BookUser bookUser, String password);

    Optional<BookUser> changePw(BookUser bookUser, String hashedPassword);

    Optional<BookUser> changeCourse(BookUser bookUser, Integer newSeqCourse);

    Optional<BookUser> changeDiscord(BookUser bookUser, String newDiscord);
}
