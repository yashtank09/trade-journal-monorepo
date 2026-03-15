package org.tradebook.journal.features.ingestion.service;

import org.tradebook.journal.features.ingestion.entity.TradeSummary;

import java.util.List;

public interface TradeSummaryService {

    List<TradeSummary> getTradeSummaries(Long userId);
}
