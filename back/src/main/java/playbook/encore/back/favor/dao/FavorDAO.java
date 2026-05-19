package playbook.encore.back.favor.dao;

import playbook.encore.back.favor.dto.FavorResponseDto;
import playbook.encore.back.bookUser.entity.BookUser;

import java.util.List;

public interface FavorDAO {
    List<FavorResponseDto> getFavors(BookUser user);

    void addFavor(BookUser user, int bookId);

    void deleteFavor(BookUser user, int bookId);
}
