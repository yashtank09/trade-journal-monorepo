package org.tradebook.journal.features.ingestion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tradebook.journal.features.ingestion.entity.TradeSummary;
import org.tradebook.journal.features.ingestion.repository.TradeSummaryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeSummaryServiceImpl implements TradeSummaryService {

    private final TradeSummaryRepository tradeSummaryRepository;

    @Override
    public List<TradeSummary> getTradeSummaries(Long userId) {
        return tradeSummaryRepository.findByUserIdOrderByOpenedAtDesc(userId);
    }
}
