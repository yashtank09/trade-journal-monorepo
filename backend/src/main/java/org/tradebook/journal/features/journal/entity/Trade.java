package org.tradebook.journal.features.journal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.tradebook.journal.common.entity.BaseEntity;
import org.tradebook.journal.features.auth.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades", indexes = {
        @Index(name = "idx_trade_date_user", columnList = "user_id, trade_date"),
        @Index(name = "idx_trade_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Trade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(length = 10, nullable = false)
    private String direction; // LONG / SHORT

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(length = 15, nullable = false)
    private String status; // OPEN, CLOSED, CANCELLED

    // P/L Data
    @Column(name = "entry_price", precision = 19, scale = 4)
    private BigDecimal entryPrice;

    @Column(name = "exit_price", precision = 19, scale = 4)
    private BigDecimal exitPrice;

    @Column(name = "gross_pnl", precision = 19, scale = 2)
    private BigDecimal grossPnl;

    @Column(name = "fees", precision = 10, scale = 2)
    private BigDecimal fees;

    @Column(name = "net_pnl", precision = 19, scale = 2)
    private BigDecimal netPnl;

    // Timing
    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;
}
