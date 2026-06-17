package org.tradebook.journal.features.ingestion.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TradeBookCsvDto {

    @CsvBindByName(column = "symbol")
    private String symbol;

    @CsvBindByName(column = "isin")
    private String isin;

    @CsvBindByName(column = "trade_date")
    @CsvDate("yyyy-MM-dd")
    private LocalDate tradeDate;

    @CsvBindByName(column = "exchange")
    private String exchange;

    @CsvBindByName(column = "segment")
    private String segment;

    @CsvBindByName(column = "series")
    private String series;

    @CsvBindByName(column = "trade_type")
    private String tradeType;

    @CsvBindByName(column = "auction")
    private Boolean auction;

    @CsvBindByName(column = "quantity")
    private BigDecimal quantity;

    @CsvBindByName(column = "price")
    private BigDecimal price;

    @CsvBindByName(column = "trade_id")
    private Long tradeId;

    @CsvBindByName(column = "order_id")
    private Long orderId;

    @CsvBindByName(column = "order_execution_time")
    @CsvDate("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderExecutionTime;

    @CsvBindByName(column = "expiry_date")
    @CsvDate("yyyy-MM-dd")
    private LocalDate expiryDate;
}
