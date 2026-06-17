package org.tradebook.journal.features.journal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.features.journal.entity.TradePlan;

import java.util.List;

@Repository
public interface TradePlanRepository extends JpaRepository<TradePlan, Long> {
    List<TradePlan> findByTradeSummaryId(Long tradeSummaryId);
}
