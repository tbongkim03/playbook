package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.SortFirst;

import java.util.List;

public interface SortFirstDAO {
    SortFirst insertSortFirst(SortFirst sortFirst);

    List<SortFirst> selectAllSortFirst() throws Exception;

    SortFirst updateSortFirst(SortFirst sortFirst) throws Exception;

    void deleteSortFirst(SortFirst sortFirst) throws Exception;
}
