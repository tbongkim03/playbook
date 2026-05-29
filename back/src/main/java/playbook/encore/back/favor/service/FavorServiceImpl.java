package playbook.encore.back.favor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.favor.dao.FavorDAO;
import playbook.encore.back.favor.dto.FavorResponseDto;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.favor.service.FavorService;

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
    @Transactional(readOnly = true)
    public List<FavorResponseDto> getFavorList(BookUser user) {
        log.info("[FavorService] 즐겨찾기 목록 조회 - userId: {}", user.getIdUser());
        return favorDAO.getFavors(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavor(BookUser user, int bookId) {
        log.info("[FavorService] 즐겨찾기 추가 - userId: {}, bookId: {}", user.getIdUser(), bookId);
        favorDAO.addFavor(user, bookId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFavor(BookUser user, int bookId) {
        log.info("[FavorService] 즐겨찾기 삭제 - userId: {}, bookId: {}", user.getIdUser(), bookId);
        favorDAO.deleteFavor(user, bookId);
    }
}
