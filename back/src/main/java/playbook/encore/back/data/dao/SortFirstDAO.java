package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.SortFirst;

import java.util.List;

public interface SortFirstDAO {
    SortFirst insertSortFirst(SortFirst sortFirst);

    List<SortFirst> selectAllSortFirst() throws Exception;

    SortFirst updateSortFirst(Integer sortFirstId, String korSortFirst, String nameSortFitst) throws Exception;

    void deleteSortFirst(SortFirst sortFirst) throws Exception;
}
