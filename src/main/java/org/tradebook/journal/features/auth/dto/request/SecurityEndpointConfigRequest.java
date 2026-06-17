package org.tradebook.journal.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tradebook.journal.features.auth.enums.AccessLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEndpointConfigRequest {

    @NotBlank(message = "Path must not be blank")
    private String path;

    @NotNull(message = "Access level must not be null")
    private AccessLevel accessLevel;

    private Boolean isActive;

    private String description;
}
