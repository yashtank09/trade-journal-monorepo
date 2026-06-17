package org.tradebook.journal.features.journal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tradebook.journal.common.exception.TradeBookException;
import org.tradebook.journal.features.auth.entity.User;
import org.tradebook.journal.features.auth.repository.UserRepository;
import org.tradebook.journal.features.journal.dto.request.CreateTradeRequest;
import org.tradebook.journal.features.journal.dto.request.UpdateTradeRequest;
import org.tradebook.journal.features.journal.dto.response.TradeDetailResponse;
import org.tradebook.journal.features.journal.dto.response.TradeSummaryResponse;
import org.tradebook.journal.features.journal.entity.Instrument;
import org.tradebook.journal.features.journal.entity.Trade;
import org.tradebook.journal.features.journal.entity.TradePlan;
import org.tradebook.journal.features.journal.repository.InstrumentRepository;
import org.tradebook.journal.features.journal.repository.TradePlanRepository;
import org.tradebook.journal.features.journal.repository.TradeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;
    private final TradePlanRepository tradePlanRepository;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;

    @Override
    @Transactional
    public TradeDetailResponse createTrade(Long userId, CreateTradeRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TradeBookException("User not found"));
        Instrument instrument = instrumentRepository.findBySymbolAndExchange(request.getSymbol(), request.getExchange()).orElseGet(() -> {
            Instrument newInstrument = Instrument.builder().symbol(request.getSymbol()).exchange(request.getExchange() != null ? request.getExchange() : "NSE").type(request.getType() != null ? request.getType() : "EQUITY").build();
            return instrumentRepository.save(newInstrument);
        });

        LocalDate tradeDate = request.getTradeDate() != null ? request.getTradeDate() : LocalDate.now();
        LocalDateTime entryTime = request.getEntryTime() != null ? request.getEntryTime() : tradeDate.atStartOfDay();

        Trade trade = Trade.builder().user(user).instrument(instrument).tradeDate(tradeDate).direction(request.getDirection()).quantity(request.getQuantity()).entryPrice(request.getEntryPrice()).entryTime(entryTime).status("OPEN").build();
        trade = tradeRepository.save(trade);

        // Deprecated flow: TradePlan now attaches to TradeSummary
        // if (request.getPlan() != null) {
        //     TradePlan plan = TradePlan.builder().trade(trade).targetPrice(request.getPlan().getTargetPrice()).stopLoss(request.getPlan().getStopLoss()).setupReason(request.getPlan().getSetupNote()).build();
        //     tradePlanRepository.save(plan);
        // }

        return mapToDetailResponse(trade);
    }

    @Override
    @Transactional
    public TradeDetailResponse updateTrade(Long tradeId, Long userId, UpdateTradeRequest request) {
        Trade trade = tradeRepository.findById(tradeId).orElseThrow(() -> new TradeBookException("Trade not found"));
        if (!trade.getUser().getId().equals(userId)) {
            throw new TradeBookException("Unauthorized access to trade");
        }

        if (request.getStatus() != null) trade.setStatus(request.getStatus());
        if (request.getExitPrice() != null) trade.setExitPrice(request.getExitPrice());
        if (request.getExitTime() != null) trade.setExitTime(request.getExitTime());
        if (request.getFees() != null) trade.setFees(request.getFees());

        if ("CLOSED".equalsIgnoreCase(trade.getStatus()) && trade.getExitPrice() != null && trade.getEntryPrice() != null) {
            BigDecimal exitVal = trade.getExitPrice().multiply(trade.getQuantity());
            BigDecimal entryVal = trade.getEntryPrice().multiply(trade.getQuantity());
            BigDecimal grossPnl = "LONG".equalsIgnoreCase(trade.getDirection()) ? exitVal.subtract(entryVal) : entryVal.subtract(exitVal);
            trade.setGrossPnl(grossPnl);
            BigDecimal fees = trade.getFees() != null ? trade.getFees() : BigDecimal.ZERO;
            trade.setNetPnl(grossPnl.subtract(fees));
        }

        tradeRepository.save(trade);
        return mapToDetailResponse(trade);
    }

    @Override
    @Transactional(readOnly = true)
    public TradeDetailResponse getTrade(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findById(tradeId).orElseThrow(() -> new TradeBookException("Trade not found"));
        if (!trade.getUser().getId().equals(userId)) {
            throw new TradeBookException("Unauthorized access to trade");
        }
        return mapToDetailResponse(trade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeSummaryResponse> getTrades(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Trade> trades = tradeRepository.findByUserIdAndTradeDateBetween(userId, startDate, endDate);
        return trades.stream().map(this::mapToSummaryResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeSummaryResponse> getOpenTrades(Long userId) {
        List<Trade> trades = tradeRepository.findByUserIdAndStatus(userId, "OPEN");
        return trades.stream().map(this::mapToSummaryResponse).toList();
    }

    private TradeDetailResponse mapToDetailResponse(Trade trade) {
        Optional<TradePlan> planOpt = Optional.empty(); // Deprecated relation
        return TradeDetailResponse.builder()
                .id(trade.getId())
                .symbol(trade.getInstrument().getSymbol())
                .exchange(trade.getInstrument().getExchange())
                .type(trade.getInstrument().getType())
                .direction(trade.getDirection())
                .status(trade.getStatus())
                .date(trade.getTradeDate())
                .quantity(trade.getQuantity())
                .entryPrice(trade.getEntryPrice())
                .exitPrice(trade.getExitPrice())
                .grossPnl(trade.getGrossPnl())
                .netPnl(trade.getNetPnl())
                .fees(trade.getFees())
                .entryTime(trade.getEntryTime())
                .exitTime(trade.getExitTime())
                .targetPrice(planOpt.map(TradePlan::getTargetPrice).orElse(null))
                .stopLoss(planOpt.map(TradePlan::getStopLoss).orElse(null))
                .setupReason(planOpt.map(TradePlan::getSetupReason).orElse(null))
                .build();
    }

    private TradeSummaryResponse mapToSummaryResponse(Trade trade) {
        Optional<TradePlan> planOpt = Optional.empty(); // Deprecated relation
        return TradeSummaryResponse.builder()
                .id(trade.getId())
                .symbol(trade.getInstrument().getSymbol())
                .exchange(trade.getInstrument().getExchange())
                .type(trade.getInstrument().getType())
                .direction(trade.getDirection())
                .status(trade.getStatus())
                .date(trade.getTradeDate())
                .quantity(trade.getQuantity())
                .entryPrice(trade.getEntryPrice())
                .entryTime(trade.getEntryTime())
                .netPnl(trade.getNetPnl())
                .targetPrice(planOpt.map(TradePlan::getTargetPrice).orElse(null))
                .stopLoss(planOpt.map(TradePlan::getStopLoss).orElse(null))
                .setupReason(planOpt.map(TradePlan::getSetupReason).orElse(null))
                .build();
    }
}
