package playbook.encore.back.sort.dao;

import playbook.encore.back.sort.entity.SortFirst;

import java.util.List;

public interface SortFirstDAO {
    SortFirst insertSortFirst(SortFirst sortFirst);

    List<SortFirst> selectAllSortFirst() throws Exception;

    SortFirst updateSortFirst(Integer sortFirstId, String korSortFirst, String nameSortFitst) throws Exception;

    void deleteSortFirst(SortFirst sortFirst) throws Exception;
}
