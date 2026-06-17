package org.tradebook.journal.common.util;

public class TradeCalculationUtils {

    private TradeCalculationUtils() {
    }

    public static String extractOptionType(String symbol) {
        if (symbol == null) return null;
        if (symbol.endsWith("CE")) return "CE";
        if (symbol.endsWith("PE")) return "PE";
        return null;
    }

}
