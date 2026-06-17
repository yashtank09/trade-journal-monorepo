package org.tradebook.journal.features.ingestion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.common.enums.PositionDirection;
import org.tradebook.journal.common.enums.PositionStatus;
import org.tradebook.journal.features.ingestion.entity.TradeSummary;

import java.util.Collection;
import java.util.List;

@Repository
public interface TradeSummaryRepository extends JpaRepository<TradeSummary, Long> {

    List<TradeSummary> findByUserIdAndSymbolAndSegmentAndDirectionAndPositionStatusInOrderByCreatedAtAsc(Long userId, String symbol, String segment, PositionDirection direction, Collection<PositionStatus> statuses);
    List<TradeSummary> findByUserIdOrderByOpenedAtDesc(Long userId);
}
