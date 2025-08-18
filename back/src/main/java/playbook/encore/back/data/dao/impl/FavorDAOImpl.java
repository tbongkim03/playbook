package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.FavorDAO;
import playbook.encore.back.data.dto.favor.FavorResponseDto;
import playbook.encore.back.data.entity.Book;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.entity.Favor;
import playbook.encore.back.data.repository.BookRepository;
import playbook.encore.back.data.repository.FavorRepository;

import java.util.List;

@Component
public class FavorDAOImpl implements FavorDAO {
    private final FavorRepository favorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public FavorDAOImpl(FavorRepository favorRepository, BookRepository bookRepository) {
        this.favorRepository = favorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public List<FavorResponseDto> getFavors(BookUser user) {
        List<FavorResponseDto> optionalFavors = favorRepository.findFavorsByUser(user);
        if (optionalFavors.isEmpty()) {
            throw new IllegalArgumentException("즐겨찾기 목록이 비어 있습니다.");
        }
        return optionalFavors;
    }

    @Override
    public void addFavor(BookUser user, int bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다."));

        Favor favor = Favor.builder()
                .seqUser(user)
                .seqBook(book)
                .build();
        favorRepository.save(favor);
    }

    @Override
    public void deleteFavor(BookUser user, int bookId) {
        Favor favor = favorRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 즐겨찾기입니다."));
        if (!favor.getSeqUser().equals(user)) {
            throw new IllegalArgumentException("해당 즐겨찾기는 현재 사용자와 연결되어 있지 않습니다.");
        }
        favorRepository.delete(favor);
    }
}
