package org.tradebook.journal.features.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tradebook.journal.features.auth.entity.SecurityEndpointConfig;
import org.tradebook.journal.features.auth.enums.AccessLevel;
import org.tradebook.journal.features.auth.repository.SecurityEndpointConfigRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads endpoint-access rules from {@code security_endpoint_configs} table and
 * caches them in memory.  The cache is refreshed every 60 seconds via
 * {@link Scheduled}, so hot-changes to the database propagate within 1 minute
 * without requiring an application restart.
 *
 * <p>This service is consumed by both {@link org.tradebook.journal.config.security.SecurityConfig}
 * (which builds the static {@link org.springframework.security.web.SecurityFilterChain} at startup)
 * and by {@link org.tradebook.journal.config.security.JwtAuthenticationFilter}
 * (which uses it to decide whether a request should bypass JWT validation).
 */
@Service
@RequiredArgsConstructor
public class SecurityEndpointConfigService {

    private static final Logger log = LoggerFactory.getLogger(SecurityEndpointConfigService.class);

    private final SecurityEndpointConfigRepository repository;

    /** In-memory cache; populated on startup and refreshed periodically. */
    private volatile List<SecurityEndpointConfig> cachedConfigs;

    // -------------------------------------------------------------------------
    // Cache lifecycle
    // -------------------------------------------------------------------------

    @PostConstruct
    @Scheduled(fixedRateString = "${security.endpoint-config.cache-refresh-ms:60000}")
    public void loadConfigs() {
        log.info("Refreshing security endpoint configurations from database ...");
        cachedConfigs = repository.findAllByIsActiveTrue();
        log.info("Loaded {} active security endpoint configuration(s)", cachedConfigs.size());
    }

    /**
     * Forces an immediate cache reload (useful after admin CRUD operations).
     */
    public void forceReload() {
        log.info("Force-reloading security endpoint configurations");
        loadConfigs();
    }

    // -------------------------------------------------------------------------
    // Path accessors (used by SecurityConfig and JwtAuthenticationFilter)
    // -------------------------------------------------------------------------

    /** Paths that require no authentication (permit all). */
    public List<String> getPublicPaths() {
        return getPathsByAccessLevel(AccessLevel.PUBLIC);
    }

    /** Paths accessible by any authenticated user. */
    public List<String> getUserPaths() {
        return getPathsByAccessLevel(AccessLevel.USER);
    }

    /** Paths accessible only by ADMIN-role users. */
    public List<String> getAdminPaths() {
        return getPathsByAccessLevel(AccessLevel.ADMIN);
    }

    /** Returns the full cached list (all active configs). */
    public List<SecurityEndpointConfig> getAllActiveConfigs() {
        ensureLoaded();
        return cachedConfigs;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private List<String> getPathsByAccessLevel(AccessLevel level) {
        ensureLoaded();
        return cachedConfigs.stream()
                .filter(cfg -> cfg.getAccessLevel() == level)
                .map(SecurityEndpointConfig::getPath)
                .collect(Collectors.toList());
    }

    private void ensureLoaded() {
        if (cachedConfigs == null) {
            loadConfigs();
        }
    }
}
