package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.SortSecondDAO;
import playbook.encore.back.data.entity.SortSecond;
import playbook.encore.back.data.repository.SortSecondRepository;

import java.util.List;
import java.util.Optional;

@Component
public class SortSecondDAOImpl implements SortSecondDAO {

    private final SortSecondRepository sortSecondRepository;

    @Autowired
    public SortSecondDAOImpl(SortSecondRepository sortSecondRepository) {
        this.sortSecondRepository = sortSecondRepository;
    }

    @Override
    public SortSecond insertSortSecond(SortSecond sortSecond) {
        SortSecond savedSortSecond = sortSecondRepository.save(sortSecond);
        return savedSortSecond;
    }

    @Override
    public List<SortSecond> selectAllSortSecond() throws Exception {
        List<SortSecond> sortSecondList = sortSecondRepository.findAll();
        return sortSecondList;
    }

    @Override
    public SortSecond updateSortSecond(Integer sortSecondId, Integer sortFirstId, String korSortSecond, String nameSortSecond) throws Exception {
        Optional<SortSecond> optionalSortSecond = sortSecondRepository.findById(sortSecondId);

        SortSecond updatedSortSecond;
        if (optionalSortSecond.isPresent()) {
            SortSecond selectedSortSecond = optionalSortSecond.get();

            selectedSortSecond.setKorSortSecond(korSortSecond);
            selectedSortSecond.setNameSortSecond(nameSortSecond);

            updatedSortSecond = sortSecondRepository.save(selectedSortSecond);
        } else {
            throw new IllegalArgumentException("업데이트에 실패하였습니다. 해당 중분류가 존재하지 않습니다: " + korSortSecond);
        }
        return updatedSortSecond;
    }

    @Override
    public void deleteSortSecond(Integer sortSecondId) throws Exception {
        Optional<SortSecond> optionalSortSecond = sortSecondRepository.findById(sortSecondId);

        if (optionalSortSecond.isPresent()) {
            SortSecond selectedSortSecond = optionalSortSecond.get();

            sortSecondRepository.delete(selectedSortSecond);
        } else {
            throw new IllegalArgumentException("해당 중분류를 삭제에 실패했습니다: " + sortSecondId);

        }
    }
}
