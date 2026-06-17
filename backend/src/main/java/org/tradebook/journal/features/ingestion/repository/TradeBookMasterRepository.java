package org.tradebook.journal.features.ingestion.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.tradebook.journal.features.ingestion.entity.TradeBookMaster;

import java.util.List;

public interface TradeBookMasterRepository extends JpaRepository<TradeBookMaster, Long> {

    @Query("SELECT t FROM TradeBookMaster t WHERE t.isJournaled = false ORDER BY t.orderExecutionTime ASC")
    List<TradeBookMaster> findPendingTrades(Pageable pageable);
}
