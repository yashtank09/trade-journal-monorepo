package org.tradebook.journal.features.ingestion.automations.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tradebook.journal.common.enums.PositionDirection;
import org.tradebook.journal.common.enums.PositionStatus;
import org.tradebook.journal.common.enums.TradeType;
import org.tradebook.journal.features.ingestion.entity.TradeBookMaster;
import org.tradebook.journal.features.ingestion.entity.TradeSummary;
import org.tradebook.journal.features.ingestion.repository.TradeBookMasterRepository;
import org.tradebook.journal.features.ingestion.repository.TradeSummaryRepository;
import org.tradebook.journal.features.journal.repository.TradeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradePositionDiscoveryService {

    private final TradeSummaryRepository tradeSummaryRepository;
    private final TradeBookMasterRepository tradeBookMasterRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public void processTrade(TradeBookMaster trade) {
        log.info("Processing Order={}, Type={}", trade.getId(), trade.getTradeType());

        PositionDirection incomingDirection = getDirection(trade.getTradeType());
        PositionDirection targetOppositeDirection = getOppositeDirection(incomingDirection);

        List<TradeSummary> openPositions = tradeSummaryRepository.findByUserIdAndSymbolAndSegmentAndDirectionAndPositionStatusInOrderByCreatedAtAsc(
                trade.getUserId(),
                trade.getSymbol(),
                trade.getSegment(),
                targetOppositeDirection,
                List.of(PositionStatus.OPEN, PositionStatus.PARTIAL)
        );

        long qtyToProcess = Math.abs(trade.getTotalQuantity());

        for (TradeSummary position : openPositions) {
            if (qtyToProcess <= 0) break;
            long availableQty = position.getOpenQty();
            long matchQty = Math.min(qtyToProcess, availableQty);

            closePositionPortion(position, matchQty, trade.getAveragePrice(), trade.getId(), trade.getOrderExecutionTime());

            qtyToProcess -= matchQty;
        }

        if (qtyToProcess > 0) {
            createNewPosition(trade, incomingDirection, qtyToProcess);
        }

        trade.setIsJournaled(true);
        tradeBookMasterRepository.save(trade);

    }

    private void createNewPosition(TradeBookMaster trade, PositionDirection direction, long quantity) {
        TradeSummary newPos = TradeSummary.builder()
                .userId(trade.getUserId())
                .symbol(trade.getSymbol())
                .segment(trade.getSegment())
                .exchange(trade.getExchange())
                .direction(direction)
                .positionStatus(PositionStatus.OPEN)
                .entryQty(quantity)
                .openQty(quantity)
                .exitQty(0L)
                .entryAvgPrice(trade.getAveragePrice())
                .exitAvgPrice(BigDecimal.ZERO)
                .realizedPnl(BigDecimal.ZERO)
                .turnover(BigDecimal.ZERO)
                .lastTradeId(trade.getId())
                .openedAt(trade.getOrderExecutionTime())
                .build();

        tradeSummaryRepository.save(newPos);
        log.info("Opened NEW Position: Symbol={} Dir={} Qty={}", trade.getSymbol(), direction, quantity);
    }

    private void closePositionPortion(TradeSummary position, long matchQty, BigDecimal exitPrice, Long tradeId, LocalDateTime exitTime) {

        BigDecimal matchedQtyBd = BigDecimal.valueOf(matchQty);
        BigDecimal pnlChunk = calculatePnl(position, exitPrice, matchedQtyBd);
        BigDecimal turnoverChunk = exitPrice.multiply(matchedQtyBd); // Value of the exit

        BigDecimal currentPnl = position.getRealizedPnl() != null ? position.getRealizedPnl() : BigDecimal.ZERO;
        position.setRealizedPnl(currentPnl.add(pnlChunk));

        BigDecimal currentTurnover = position.getTurnover() != null ? position.getTurnover() : BigDecimal.ZERO;
        position.setTurnover(currentTurnover.add(turnoverChunk));

        long oldExitQty = position.getExitQty();
        long newTotalExitQty = oldExitQty + matchQty;

        BigDecimal oldTotalExitVal = position.getExitAvgPrice().multiply(BigDecimal.valueOf(oldExitQty));
        BigDecimal newExitVal = exitPrice.multiply(matchedQtyBd);

        BigDecimal newAvgExitPrice = oldTotalExitVal.add(newExitVal).divide(BigDecimal.valueOf(newTotalExitQty), 4, RoundingMode.HALF_UP);

        position.setExitAvgPrice(newAvgExitPrice);
        position.setExitQty(newTotalExitQty);

        position.setOpenQty(position.getOpenQty() - matchQty);

        if (position.getOpenQty() == 0) {
            position.setPositionStatus(PositionStatus.CLOSED);
            position.setClosedAt(exitTime);
        } else {
            position.setPositionStatus(PositionStatus.PARTIAL);
        }

        position.setLastTradeId(tradeId);

        tradeSummaryRepository.save(position);
        log.info("Closed Portion: ID={} MatchQty={} PnL={}", position.getId(), matchQty, pnlChunk);
    }


    private BigDecimal calculatePnl(TradeSummary position, BigDecimal currentPrice, BigDecimal qty) {
        BigDecimal entryVal = position.getEntryAvgPrice().multiply(qty);
        BigDecimal exitVal = currentPrice.multiply(qty);

        if (position.getDirection() == PositionDirection.LONG) {
            return exitVal.subtract(entryVal);
        } else {
            return entryVal.subtract(exitVal);
        }
    }

    private PositionDirection getDirection(String tradeType) {
        return TradeType.BUY.getValue().equalsIgnoreCase(tradeType) ? PositionDirection.LONG : PositionDirection.SHORT;
    }

    private PositionDirection getOppositeDirection(PositionDirection dir) {
        return dir == PositionDirection.LONG ? PositionDirection.SHORT : PositionDirection.LONG;
    }
}
