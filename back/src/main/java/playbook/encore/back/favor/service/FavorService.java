package playbook.encore.back.favor.service;

import playbook.encore.back.favor.dto.FavorResponseDto;
import playbook.encore.back.bookUser.entity.BookUser;

import java.util.List;

public interface FavorService {
    List<FavorResponseDto> getFavorList(BookUser user);

    void addFavor(BookUser user, int bookId);

    void deleteFavor(BookUser user, int bookId);
}
