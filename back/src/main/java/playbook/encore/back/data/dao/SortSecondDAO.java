package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.SortSecond;

import java.util.List;

public interface SortSecondDAO {
    SortSecond insertSortSecond(SortSecond sortSecond);

    List<SortSecond> selectAllSortSecond() throws Exception;

    SortSecond updateSortSecond(SortSecond sortSecond) throws Exception;

    void deleteSortSecond(SortSecond sortSecond) throws Exception;
}
