package org.tradebook.journal.common.enums;

public enum Exchange {
    NSE("NSE"),
    BSE("BSE"),
    MCX("MCX");

    private final String value;

    Exchange(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Exchange from(String value) {
        return Exchange.valueOf(value);
    }

}
