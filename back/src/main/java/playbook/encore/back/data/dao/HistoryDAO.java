package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.History;

public interface HistoryDAO {
    void bookBorrow(History history);
    void bookReturn(History history);
}
