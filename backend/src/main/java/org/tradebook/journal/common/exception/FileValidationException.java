package org.tradebook.journal.common.exception;

public class FileValidationException extends TradeBookException {
    public FileValidationException(String message) {
        super(message);
    }
    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

