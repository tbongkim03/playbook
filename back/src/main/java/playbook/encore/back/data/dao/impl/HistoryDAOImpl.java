package playbook.encore.back.data.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import playbook.encore.back.data.dao.HistoryDAO;
import playbook.encore.back.data.entity.History;
import playbook.encore.back.data.repository.HistoryRepository;

@Component
public class HistoryDAOImpl implements HistoryDAO {

    private final HistoryRepository historyRepository;

    @Autowired
    public HistoryDAOImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public void bookBorrow(History history) {
        historyRepository.save(history);
    }

    @Override
    public void bookReturn(History history) {
        historyRepository.save(history);
    }
}
