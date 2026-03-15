package org.tradebook.journal.features.journal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.features.journal.entity.Trade;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserIdAndTradeDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Trade> findByUserIdAndStatus(Long userId, String status);
}
