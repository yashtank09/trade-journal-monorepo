package org.tradebook.journal.features.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.tradebook.journal.common.entity.BaseEntity;
import org.tradebook.journal.common.enums.TradeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "trade_book_master",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_trade_execution",
                        columnNames = {
                                "user_id",
                                "symbol",
                                "order_execution_time",
                                "trade_type",
                                "order_id"
                        }
                )
        },
        indexes = {
                @Index(name = "idx_trade_date", columnList = "user_id, trade_date"),
                @Index(name = "idx_symbol", columnList = "user_id, symbol"),
                @Index(name = "idx_order_execution_time", columnList = "user_id, order_execution_time")
        }
)
@Builder
public class TradeBookMaster extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "option_type", nullable = false, length = 2)
    private String optionType; // CE / PE

    @Column(name = "exchange", nullable = false, length = 10)
    private String exchange;

    @Column(name = "segment", nullable = false, length = 10)
    private String segment;

    @Column(name = "status", length = 15)
    @Enumerated(EnumType.STRING)
    private TradeStatus status; // OPEN, CLOSED, PARTIAL

    @Column(name = "trade_type", nullable = false, length = 10)
    private String tradeType;

    @Column(name = "total_quantity", nullable = false)
    private Long totalQuantity;

    @Column(name = "strike_price", precision = 10, scale = 2)
    private BigDecimal strikePrice;

    @Column(name = "vwap", precision = 10, scale = 4)
    private BigDecimal vwap;

    @Column(name = "average_price", nullable = false, precision = 10, scale = 4)
    private BigDecimal averagePrice;

    @Column(name = "order_execution_time", nullable = false)
    private LocalDateTime orderExecutionTime;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "is_journaled")
    private Boolean isJournaled = false;
}