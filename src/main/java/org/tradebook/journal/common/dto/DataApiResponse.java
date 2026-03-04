package org.tradebook.journal.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Generic API response wrapper for all API responses
 *
 * @param <T> Type of the data being returned in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper containing status, message, and optional data")
public class DataApiResponse<T> {
    @Schema(description = "Status of the API response (e.g., 'success', 'error')",
            example = "success",
            allowableValues = {"success", "error"})
    @JsonProperty("status")
    private String status;

    @Schema(description = "HTTP status code of the response", example = "200")
    @JsonProperty("status-code")
    private int statusCode;

    @Schema(description = "Human-readable message describing the response",
            example = "Operation completed successfully")
    @JsonProperty("status-message")
    private String statusMessage;

    @Schema(description = "JWT token for authenticated requests (if applicable)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("token")
    private String token;

    @Schema(description = "The actual data payload of the response (if any)",
            nullable = true)
    @JsonProperty("data")
    private T data;

    @Schema(description = "Timestamp of the API response (UTC)")
    @JsonProperty("timestamp")
    private Instant timestamp = Instant.now(); // Default value is now()

    public DataApiResponse(String status, int statusCode, String statusMessage, T data) {
        this.status = status;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public DataApiResponse(String status, int statusCode, String statusMessage) {
        this.status = status;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.timestamp = Instant.now();
    }

    public DataApiResponse(String status, int statusCode, String statusMessage, String token, T data) {
        this.status = status;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.token = token;
        this.timestamp = Instant.now();
        this.data = data;
    }

}
