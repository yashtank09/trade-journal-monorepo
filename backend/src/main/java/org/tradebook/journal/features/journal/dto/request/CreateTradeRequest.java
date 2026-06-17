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
public class CreateTradeRequest {
    private String symbol;
    private String exchange;

    // "EQUITY", "OPTION", "FUTURE"
    private String type;

    // "LONG", "SHORT"
    private String direction;

    private BigDecimal quantity;
    private BigDecimal entryPrice;
    private LocalDateTime entryTime;
    private java.time.LocalDate tradeDate;

    private CreateTradePlanRequest plan;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTradePlanRequest {
        private BigDecimal targetPrice;
        private BigDecimal stopLoss;
        private BigDecimal riskRewardRatio; // Optional, or calculated
        private String setupNote;
    }
}
