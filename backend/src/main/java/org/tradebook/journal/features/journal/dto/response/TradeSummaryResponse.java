package org.tradebook.journal.features.journal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeSummaryResponse {
    private Long id;
    private String symbol;
    private String exchange;
    private String type;
    private String direction;
    private String status;
    private LocalDate date;
    private BigDecimal quantity;
    private BigDecimal entryPrice;
    private LocalDateTime entryTime;
    private BigDecimal netPnl;

    // From TradePlan
    private BigDecimal targetPrice;
    private BigDecimal stopLoss;
    private String setupReason;
}
