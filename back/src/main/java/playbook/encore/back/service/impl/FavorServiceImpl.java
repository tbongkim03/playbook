package playbook.encore.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.data.dao.FavorDAO;
import playbook.encore.back.data.dto.favor.FavorResponseDto;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.service.FavorService;

import java.util.List;

@Slf4j
@Service
public class FavorServiceImpl implements FavorService {

    private final FavorDAO favorDAO;

    @Autowired
    public FavorServiceImpl(FavorDAO favorDAO) {
        this.favorDAO = favorDAO;
    }

    @Override
    public List<FavorResponseDto> getFavorList(BookUser user) {
        log.info("[FavorService] 즐겨찾기 목록 조회 - userId: {}", user.getIdUser());
        List<FavorResponseDto> favors = favorDAO.getFavors(user);
        if (favors.isEmpty()) {
            throw new IllegalArgumentException("즐겨찾기 목록이 비어 있습니다.");
        }
        return favors;
    }

    @Override
    public void addFavor(BookUser user, int bookId) {
        log.info("[FavorService] 즐겨찾기 추가 - userId: {}, bookId: {}", user.getIdUser(), bookId);
        favorDAO.addFavor(user, bookId);
    }

    @Override
    @Transactional
    public void deleteFavor(BookUser user, int bookId) {
        log.info("[FavorService] 즐겨찾기 삭제 - userId: {}, bookId: {}", user.getIdUser(), bookId);
        favorDAO.deleteFavor(user, bookId);
    }
}
