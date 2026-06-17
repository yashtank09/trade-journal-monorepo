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
public class RiskMetricsDTO {
    private BigDecimal averageRiskAmount;
    private double averageRMultiple;
    private BigDecimal maxDrawdown;
    private double expectancy;
}
