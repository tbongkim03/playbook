package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.SortFirstDAO;
import playbook.encore.back.data.entity.SortFirst;
import playbook.encore.back.data.repository.SortFirstRepository;

import java.util.List;
import java.util.Optional;

@Component
public class SortFirstDAOImpl implements SortFirstDAO {

    private final SortFirstRepository sortFirstRepository;

    @Autowired
    public SortFirstDAOImpl(SortFirstRepository sortFirstRepository) {
        this.sortFirstRepository = sortFirstRepository;
    }

    @Override
    public SortFirst insertSortFirst(SortFirst sortFirst) {
        SortFirst savedSortFirst = sortFirstRepository.save(sortFirst);
        return savedSortFirst;
    }

    @Override
    public List<SortFirst> selectAllSortFirst() throws Exception {
        List<SortFirst> sortFirstList = sortFirstRepository.findAll();
        return sortFirstList;
    }

    @Override
    public SortFirst updateSortFirst(Integer sortFirstId, String korSortFirst, String nameSortFirst) throws Exception {
        Optional<SortFirst> optionalSortFirst = sortFirstRepository.findById(sortFirstId);

        SortFirst updatedSortFirst;
        if (optionalSortFirst.isPresent()) {
            SortFirst selectedSortFirst = optionalSortFirst.get();

            selectedSortFirst.setKorSortFirst(korSortFirst);
            selectedSortFirst.setNameSortFirst(nameSortFirst);

            updatedSortFirst = sortFirstRepository.save(selectedSortFirst);
        } else {
            throw new IllegalArgumentException("업데이트에 실패하였습니다. 해당 대분류가 존재하지 않습니다: " + nameSortFirst);
        }
        return updatedSortFirst;
    }

    @Override
    public void deleteSortFirst(SortFirst sortFirst) throws Exception {
        Optional<SortFirst> optionalSortFirst = sortFirstRepository.findById(sortFirst.getSeqSortFirst());

        if (optionalSortFirst.isPresent()) {
            SortFirst selectedSortFirst = optionalSortFirst.get();

            sortFirstRepository.delete(selectedSortFirst);
        } else {
            throw new IllegalArgumentException("해당 대분류를 삭제에 실패했습니다: " + sortFirst.getSeqSortFirst());
        }
    }
}
