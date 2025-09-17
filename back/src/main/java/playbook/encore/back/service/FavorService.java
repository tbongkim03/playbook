package playbook.encore.back.service;

import playbook.encore.back.data.dto.favor.FavorResponseDto;
import playbook.encore.back.data.entity.BookUser;

import java.util.List;

public interface FavorService {
    List<FavorResponseDto> getFavorList(BookUser user);

    void addFavor(BookUser user, int bookId);

    void deleteFavor(BookUser user, int bookId);
}
