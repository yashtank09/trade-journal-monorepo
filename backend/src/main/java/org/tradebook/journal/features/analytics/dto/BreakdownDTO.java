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
public class BreakdownDTO {
    private String category;
    private BigDecimal netPnl;
    private int tradeCount;
    private double winRate;
}
