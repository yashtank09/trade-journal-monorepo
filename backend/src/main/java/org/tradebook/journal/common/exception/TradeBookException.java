package org.tradebook.journal.common.exception;

public class TradeBookException extends RuntimeException {
    public TradeBookException(String message) {
        super(message);
    }

    public TradeBookException(String message, Throwable cause) {
        super(message, cause);
    }
}