package playbook.encore.back.data.dao.impl;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import playbook.encore.back.data.dao.BookUserDAO;
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
    public BookUser searchBookUserResultExact(String idUser) {
        return bookUserRepository.findByIdUser(idUser)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다 : " + idUser));
    }
    
}
