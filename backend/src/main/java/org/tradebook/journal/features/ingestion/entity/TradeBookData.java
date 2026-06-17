package org.tradebook.journal.features.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.tradebook.journal.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trade_book_data",
        indexes = {
                // Processing Grouping (Used by fetchAggregatedTrades)
                // Includes user_id so we group strictly by user
                @Index(name = "idx_process_group", columnList = "processed_flag, user_id, symbol, order_id"),

                // Deduplication Check (Used by findExistingTradeIds)
                // Crucial: Checks if THIS user already has this trade_id
                @Index(name = "idx_dedupe_check", columnList = "user_id, trade_id"),

                // Update Flag (Used by updateProcessedRecords)
                // Find rows to mark complete for a specific user
                @Index(name = "idx_update_process", columnList = "user_id, symbol, order_id"),

                // Analytics / Filtering helpers
                @Index(name = "idx_user_symbol", columnList = "user_id, symbol"),
                @Index(name = "idx_user_date", columnList = "user_id, trade_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeBookData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "trade_id", nullable = false)
    private Long tradeId;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "isin")
    private String isin;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "exchange")
    private String exchange;

    @Column(name = "segment")
    private String segment;

    @Column(name = "series")
    private String series;

    @Column(name = "trade_type", nullable = false)
    private String tradeType;

    @Column(name = "auction")
    private Boolean auction;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", precision = 10, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "processed_flag", nullable = false)
    private Boolean processedFlag = false;

    @Column(name = "processed_batch_id", columnDefinition = "BINARY(16)")
    private UUID processedBatchId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "source_file_id", nullable = false)
    private Long sourceFileId;

    @Column(name = "order_execution_time", nullable = false)
    private LocalDateTime orderExecutionTime;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
}