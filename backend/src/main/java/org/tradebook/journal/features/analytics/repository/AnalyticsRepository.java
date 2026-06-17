package org.tradebook.journal.features.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.features.analytics.dto.BreakdownDTO;
import org.tradebook.journal.features.ingestion.entity.TradeSummary;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<TradeSummary, Long> {

    @Query("SELECT ts FROM TradeSummary ts WHERE ts.userId = :userId AND ts.openedAt >= :startDate AND ts.openedAt <= :endDate ORDER BY ts.openedAt ASC")
    List<TradeSummary> findTradeSummariesForAnalytics(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new org.tradebook.journal.features.analytics.dto.BreakdownDTO(ts.segment, COALESCE(SUM(ts.realizedPnl), 0), CAST(COUNT(ts.id) AS int), 0.0) " +
           "FROM TradeSummary ts " +
           "WHERE ts.userId = :userId AND ts.openedAt >= :startDate AND ts.openedAt <= :endDate " +
           "GROUP BY ts.segment")
    List<BreakdownDTO> getAssetBreakdown(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new org.tradebook.journal.features.analytics.dto.BreakdownDTO(COALESCE(tp.strategy, 'None'), COALESCE(SUM(ts.realizedPnl), 0), CAST(COUNT(ts.id) AS int), 0.0) " +
           "FROM TradeSummary ts LEFT JOIN TradePlan tp ON tp.tradeSummary.id = ts.id " +
           "WHERE ts.userId = :userId AND ts.openedAt >= :startDate AND ts.openedAt <= :endDate " +
           "GROUP BY COALESCE(tp.strategy, 'None')")
    List<BreakdownDTO> getStrategyBreakdown(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT tp FROM TradePlan tp JOIN tp.tradeSummary ts WHERE ts.userId = :userId AND ts.openedAt >= :startDate AND ts.openedAt <= :endDate")
    List<org.tradebook.journal.features.journal.entity.TradePlan> findTradePlansForAnalytics(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
