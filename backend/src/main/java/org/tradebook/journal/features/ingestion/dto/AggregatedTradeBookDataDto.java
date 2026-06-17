package org.tradebook.journal.features.ingestion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AggregatedTradeBookDataDto {
    private String symbol;
    private Long userId;
    private Long orderId;
    private String exchange;
    private LocalDate expiryDate;
    private String isin;
    private String segment;
    private String series;
    private String tradeType;
    private LocalDate tradeDate;
    private LocalDateTime orderExecutionTime;
    private Long totalQuantity;
    private BigDecimal averagePrice;
    private BigDecimal vwap;

    public AggregatedTradeBookDataDto(String symbol, Long userId, Long orderId, String exchange, LocalDate expiryDate, String isin,
                                      String segment, String series, String tradeType, LocalDate tradeDate, LocalDateTime orderExecutionTime,
                                      Long totalQuantity, BigDecimal averagePrice, BigDecimal vwap) {
        this.symbol = symbol;
        this.userId = userId;
        this.orderId = orderId;
        this.exchange = exchange;
        this.expiryDate = expiryDate;
        this.isin = isin;
        this.segment = segment;
        this.series = series;
        this.tradeType = tradeType;
        this.tradeDate = tradeDate;
        this.orderExecutionTime = orderExecutionTime;
        this.totalQuantity = totalQuantity;
        this.averagePrice = averagePrice;
        this.vwap = vwap;
    }
}
