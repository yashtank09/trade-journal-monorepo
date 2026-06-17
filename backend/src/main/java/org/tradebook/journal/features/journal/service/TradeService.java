package org.tradebook.journal.features.journal.service;

import org.tradebook.journal.features.journal.dto.request.CreateTradeRequest;
import org.tradebook.journal.features.journal.dto.request.UpdateTradeRequest;
import org.tradebook.journal.features.journal.dto.response.TradeDetailResponse;
import org.tradebook.journal.features.journal.dto.response.TradeSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface TradeService {

    TradeDetailResponse createTrade(Long userId, CreateTradeRequest request);

    TradeDetailResponse updateTrade(Long tradeId, Long userId, UpdateTradeRequest request);

    TradeDetailResponse getTrade(Long tradeId, Long userId);

    List<TradeSummaryResponse> getTrades(Long userId, LocalDate startDate, LocalDate endDate);

    List<TradeSummaryResponse> getOpenTrades(Long userId);
}
