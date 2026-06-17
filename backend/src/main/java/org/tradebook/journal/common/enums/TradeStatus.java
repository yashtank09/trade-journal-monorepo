package org.tradebook.journal.common.enums;

public enum TradeStatus {
    OPEN("OPEN"),
    CLOSED("CLOSED"),
    PARTIAL("PARTIAL");

    private final String value;

    TradeStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TradeStatus from(String value) {
        return TradeStatus.valueOf(value.toUpperCase());
    }
}
