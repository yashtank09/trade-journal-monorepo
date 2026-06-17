package org.tradebook.journal.features.journal.service;

import org.tradebook.journal.features.journal.entity.TradePlan;
import java.util.List;

public interface TradePlanService {
    TradePlan save(TradePlan tradePlan);
    List<TradePlan> findByTradeSummaryId(Long tradeSummaryId);
}
