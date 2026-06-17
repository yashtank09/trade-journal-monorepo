package org.tradebook.journal.features.cors.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Response payload representing a CORS policy record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorsPolicyResponse {

    private Long id;
    private String allowedOrigin;
    private String allowedMethods;
    private String allowedHeaders;
    private String exposedHeaders;
    private Boolean allowCredentials;
    private Long maxAge;
    private Boolean isActive;
    private String description;

    // Audit fields inherited from BaseEntity
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
