package org.tradebook.journal.features.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tradebook.journal.features.auth.enums.AccessLevel;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEndpointConfigResponse {

    private Long id;
    private String path;
    private AccessLevel accessLevel;
    private Boolean isActive;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
