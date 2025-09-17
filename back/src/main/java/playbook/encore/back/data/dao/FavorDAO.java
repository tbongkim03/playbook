package playbook.encore.back.data.dao;

import playbook.encore.back.data.dto.favor.FavorResponseDto;
import playbook.encore.back.data.entity.BookUser;

import java.util.List;

public interface FavorDAO {
    List<FavorResponseDto> getFavors(BookUser user);

    void addFavor(BookUser user, int bookId);

    void deleteFavor(BookUser user, int bookId);
}
