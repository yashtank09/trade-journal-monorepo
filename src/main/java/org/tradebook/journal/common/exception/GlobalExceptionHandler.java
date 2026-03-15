package org.tradebook.journal.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.tradebook.journal.common.dto.DataApiResponse;

import static org.tradebook.journal.common.constants.ApiConstants.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<DataApiResponse<Void>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(DataApiResponse.error(CODE_BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<DataApiResponse<Void>> handleFileValidationException(FileValidationException ex) {
        return ResponseEntity
                .badRequest()
                .body(DataApiResponse.error(CODE_BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(FileStorageExcpetion.class)
    public ResponseEntity<DataApiResponse<Void>> handleFileStorageException(FileStorageExcpetion ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DataApiResponse.error(CODE_INTERNAL_ERROR, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataApiResponse<Void>> handleGlobalException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DataApiResponse.error(CODE_INTERNAL_ERROR, "An unexpected error occurred: " + ex.getMessage()));
    }
}
