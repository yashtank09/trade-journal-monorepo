package org.tradebook.journal.features.cors.entity;

import jakarta.persistence.*;
import lombok.*;
import org.tradebook.journal.common.entity.BaseEntity;

/**
 * Entity representing a dynamic CORS configuration.
 * Extends {@link BaseEntity} to inherit JPA Auditing metadata (created_at, updated_at, created_by, updated_by).
 */
@Entity
@Table(
        name = "cors_policies",
        uniqueConstraints = @UniqueConstraint(name = "uk_cors_origin", columnNames = "allowed_origin")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CorsPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "allowed_origin", nullable = false, unique = true, length = 255)
    private String allowedOrigin;

    @Column(name = "allowed_methods", nullable = false, length = 255)
    private String allowedMethods; // Comma-separated methods, e.g., "GET,POST,PUT,DELETE,OPTIONS"

    @Column(name = "allowed_headers", nullable = false, length = 500)
    private String allowedHeaders; // Comma-separated headers, e.g., "*", or specific headers

    @Column(name = "exposed_headers", length = 500)
    private String exposedHeaders; // Comma-separated headers exposed to the client browser

    @Column(name = "allow_credentials", nullable = false)
    @Builder.Default
    private Boolean allowCredentials = true;

    @Column(name = "max_age", nullable = false)
    @Builder.Default
    private Long maxAge = 3600L;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
