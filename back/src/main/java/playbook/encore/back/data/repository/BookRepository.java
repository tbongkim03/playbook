package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.data.entity.Book;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Integer countByIsbnBook(String isbnBook);
}
