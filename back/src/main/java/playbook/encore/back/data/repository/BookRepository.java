package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.data.entity.Book;

public interface BookRepository extends JpaRepository<Book, Integer> {
}
