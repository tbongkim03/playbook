package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.FavorDAO;
import playbook.encore.back.data.dto.favor.FavorResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.repository.FavorRepository;

import java.util.List;
import java.util.Optional;

@Component
public class FavorDAOImpl implements FavorDAO {
    private final FavorRepository favorRepository;

    @Autowired
    public FavorDAOImpl(FavorRepository favorRepository) {
        this.favorRepository = favorRepository;
    }

    @Override
    public List<FavorResponseDto> getFavors(BookUser user) {
        List<FavorResponseDto> optionalFavors = favorRepository.findFavorsByUser(user);
        if (optionalFavors.isEmpty()) {
            throw new IllegalArgumentException("즐겨찾기 목록이 비어 있습니다.");
        }
        return optionalFavors;
    }
}
