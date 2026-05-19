package playbook.encore.back.bookUser.dao;

import java.util.List;
import java.util.Optional;

import playbook.encore.back.bookUser.dto.LoginUserResponseDto;
import playbook.encore.back.bookUser.entity.BookUser;

public interface BookUserDAO {

    BookUser createUser(BookUser bookUser);

    Optional<BookUser> searchBookUserResultExact(String idUser);

    Optional<BookUser> loginIdPwCheck(String id, String pw);

    Optional<BookUser> pwValidate(BookUser bookUser, String password);

    Optional<BookUser> changePw(BookUser bookUser, String hashedPassword);

    Optional<BookUser> changeDiscord(BookUser bookUser, String newDiscord);

    Optional<BookUser> updateStatus(BookUser bookUser, BookUser.StatusType status);

    List<Object[]> getBookUserList();
}
