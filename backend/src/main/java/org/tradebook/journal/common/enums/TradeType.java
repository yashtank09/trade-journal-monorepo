package org.tradebook.journal.common.enums;

public enum TradeType {
    BUY("buy"),
    SELL("sell");

    private final String value;

    TradeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TradeType from(String value) {
        return TradeType.valueOf(value);
    }
}
