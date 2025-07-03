package playbook.encore.back.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import playbook.encore.back.data.entity.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Integer countByIsbnBook(String isbnBook);
    List<Book> findByTitleBookContaining(String titleBook);
}
