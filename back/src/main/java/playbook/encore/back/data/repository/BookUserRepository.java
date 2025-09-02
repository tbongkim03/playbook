package playbook.encore.back.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import playbook.encore.back.data.entity.BookUser;

public interface BookUserRepository extends JpaRepository<BookUser, Integer> {
    Optional<BookUser> findByIdUser(String idUser);

    Optional<BookUser> findByDcUser(String discordUsername);
}
