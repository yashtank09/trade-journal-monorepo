package org.tradebook.journal.features.journal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTradeRequest {
    private Long tradeId;
    private BigDecimal exitPrice;
    private LocalDateTime exitTime;
    private BigDecimal fees;
    private String status; // "CLOSED", "CANCELLED", etc.
    private String reviewNote; // To be added to TradePlan if needed, or separate review entity
}
