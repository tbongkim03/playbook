package playbook.encore.back.data.dao.impl;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.data.dto.bookUser.LoginUserResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.repository.BookUserRepository;

@Component
@RequiredArgsConstructor
public class BookUserDAOImpl implements BookUserDAO {

    private final BookUserRepository bookUserRepository;

    @Override
    public BookUser createUser(BookUser bookUser) {
        return bookUserRepository.save(bookUser);
    }

    @Override
    public Optional<BookUser> searchBookUserResultExact(String idUser) {
        return bookUserRepository.findByIdUser(idUser);
    }

    @Override
    public boolean loginIdPwCheck(String idUser, String pwUser) {
        Optional<BookUser> optionalUser = bookUserRepository.findByIdUser(idUser);
        if (optionalUser.isEmpty()) return false;
        BookUser user = optionalUser.get();
        return BCrypt.checkpw(pwUser, user.getPwUser());
    }
    
}
