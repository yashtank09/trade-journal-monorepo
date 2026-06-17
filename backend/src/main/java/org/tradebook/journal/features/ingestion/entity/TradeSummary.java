package org.tradebook.journal.features.ingestion.entity;


import jakarta.persistence.*;
import lombok.*;
import org.tradebook.journal.common.entity.BaseEntity;
import org.tradebook.journal.common.enums.PositionDirection;
import org.tradebook.journal.common.enums.PositionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "trade_summary",
        indexes = {
                @Index(name = "idx_summary_symbol_segment", columnList = "symbol, segment"),
                @Index(name = "idx_summary_position_status", columnList = "position_status"),
                @Index(name = "idx_summary_last_trade", columnList = "last_trade_id")
        }
)
public class TradeSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;

    @Column(name = "segment", nullable = false, length = 10)
    private String segment;

    @Column(name = "exchange", length = 20)
    private String exchange;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private PositionDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_status", nullable = false, length = 10)
    private PositionStatus positionStatus;

    @Column(name = "entry_qty", nullable = false)
    private Long entryQty;

    @Column(name = "entry_avg_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal entryAvgPrice;

    @Column(name = "exit_qty", nullable = false)
    private Long exitQty;

    @Column(name = "exit_avg_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal exitAvgPrice;

    @Column(name = "open_qty", nullable = false)
    private Long openQty;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "realized_pnl", precision = 19, scale = 2)
    private BigDecimal realizedPnl;

    @Column(name = "last_trade_id")
    private Long lastTradeId;

    @Column(name = "turnover", precision = 19, scale = 2)
    private BigDecimal turnover;
}