package playbook.encore.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import playbook.encore.back.data.dao.FavorDAO;
import playbook.encore.back.data.dto.favor.FavorResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.service.FavorService;

import java.util.List;

@Service
public class FavorServiceImpl implements FavorService {

    private final FavorDAO favorDAO;

    @Autowired
    public FavorServiceImpl(FavorDAO favorDAO) {
        this.favorDAO = favorDAO;
    }

    @Override
    public List<FavorResponseDto> getFavorList(BookUser user) {
        List<FavorResponseDto> favors = favorDAO.getFavors(user);
        if (favors.isEmpty()) {
            throw new IllegalArgumentException("즐겨찾기 목록이 비어 있습니다.");
        }
        return favors;
    }
}
