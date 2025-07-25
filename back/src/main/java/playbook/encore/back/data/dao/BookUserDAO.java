package playbook.encore.back.data.dao;

import java.util.Optional;

import playbook.encore.back.data.dto.bookUser.LoginUserResponseDto;
import playbook.encore.back.data.entity.BookUser;

public interface BookUserDAO {

    BookUser createUser(BookUser bookUser);

    Optional<BookUser> searchBookUserResultExact(String idUser);

    boolean loginIdPwCheck(String id, String pw);
    
}
