package org.tradebook.journal.features.journal.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TradeResponse {
    private Long id;
    private String symbol;
    private String exchange;
    private String currency;
    private BigDecimal price;
    private BigDecimal quantity;
    private String tradeType;
    private LocalDateTime tradeDate;
    private String status;
    private String comments;
}
