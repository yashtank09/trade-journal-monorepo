package org.tradebook.journal.features.ingestion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tradebook.journal.features.ingestion.dto.AggregatedTradeBookDataDto;
import org.tradebook.journal.features.ingestion.entity.TradeBookData;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TradeBookDataRepository extends JpaRepository<TradeBookData, Long> {
    @Query(value = """
            SELECT new org.tradebook.journal.features.ingestion.dto.AggregatedTradeBookDataDto(
                t.symbol,
                t.userId,
                t.orderId,
                MAX(t.exchange),
                MAX(t.expiryDate),
                MAX(t.isin),
                MAX(t.segment),
                MAX(t.series),
                t.tradeType,
                MAX(t.tradeDate),
                MIN(t.orderExecutionTime),
                SUM(t.quantity),
                CAST(AVG(t.price) AS BIGDECIMAL),
                CAST(SUM(t.price * t.quantity) / SUM(t.quantity) AS BIGDECIMAL)
            )
            FROM
                TradeBookData t
            WHERE
                t.processedFlag = FALSE
            GROUP BY t.userId, t.symbol , t.orderId , t.tradeType
            ORDER BY MIN(t.orderExecutionTime)
            """)
    List<AggregatedTradeBookDataDto> fetchAggregatedTrades();

    @Modifying
    @Query("""
        UPDATE TradeBookData t
            SET t.processedFlag = TRUE,
                t.processedBatchId = :batchId,
                t.processedAt = CURRENT_TIMESTAMP
            WHERE t.processedFlag = FALSE
              AND t.symbol = :symbol
              AND t.tradeType = :tradeType
              AND t.orderId = :orderId
    """)
    void updateProcessedRecords(UUID batchId, String symbol, String tradeType, Long orderId);

    @Query("""
                SELECT t.tradeId
                FROM TradeBookData t
                WHERE t.tradeId IN :tradeIds
            """)
    Set<Long> findExistingTradeIds(Set<Long> tradeIds);

}
