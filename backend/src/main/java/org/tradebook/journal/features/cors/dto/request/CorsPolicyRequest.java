package org.tradebook.journal.features.cors.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request payload for creating or updating a CORS policy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorsPolicyRequest {

    @NotBlank(message = "Allowed origin must not be blank")
    private String allowedOrigin;

    @NotBlank(message = "Allowed methods must not be blank")
    private String allowedMethods; // e.g. "GET,POST,PUT,DELETE,OPTIONS"

    @NotBlank(message = "Allowed headers must not be blank")
    private String allowedHeaders; // e.g. "*" or "Content-Type,Authorization"

    private String exposedHeaders; // e.g. "Authorization"

    @NotNull(message = "Allow credentials must not be null")
    private Boolean allowCredentials;

    @NotNull(message = "Max age must not be null")
    @Positive(message = "Max age must be a positive number of seconds")
    private Long maxAge;

    private Boolean isActive;

    private String description;
}
