package org.tradebook.journal.features.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private int totalTrades;
    private double winRate;
    private BigDecimal totalNetPnl;
    private double profitFactor;
    private BigDecimal averageWin;
    private BigDecimal averageLoss;
    private BigDecimal maxWin;
    private BigDecimal maxLoss;
}

