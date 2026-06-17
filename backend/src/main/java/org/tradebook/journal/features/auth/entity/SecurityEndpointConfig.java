package org.tradebook.journal.features.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.tradebook.journal.features.auth.enums.AccessLevel;

import java.time.Instant;

/**
 * Stores API endpoint path patterns and their required access level.
 *
 * <p>Paths use Ant-style patterns (e.g. {@code /auth/**}, {@code /trades/**})
 * and are loaded into Spring Security's {@code SecurityFilterChain} at startup.
 * The service layer caches these rows and refreshes them on a fixed schedule
 * so that changes made to this table take effect without a restart.
 *
 * <p>Example rows:
 * <pre>
 *  /auth/**              → PUBLIC
 *  /swagger-ui/**        → PUBLIC
 *  /trades/**            → USER
 *  /admin/**             → ADMIN
 * </pre>
 */
@Entity
@Table(
        name = "security_endpoint_configs",
        uniqueConstraints = @UniqueConstraint(name = "uk_sec_path", columnNames = "path")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEndpointConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ant-style URL pattern, e.g. {@code /auth/**} */
    @Column(nullable = false, unique = true, length = 500)
    private String path;

    /** Who is allowed to call this path. */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 30)
    private AccessLevel accessLevel;

    /** Toggle to quickly disable a row without deleting it. */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Human-readable note explaining why this entry exists. */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
