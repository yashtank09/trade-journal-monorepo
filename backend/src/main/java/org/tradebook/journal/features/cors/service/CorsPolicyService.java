package org.tradebook.journal.features.cors.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfiguration;
import org.tradebook.journal.features.cors.dto.CorsPolicyMatchingResult;
import org.tradebook.journal.features.cors.entity.CorsPolicy;
import org.tradebook.journal.features.cors.repository.CorsPolicyRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service that caches active CORS policies in-memory and performs dynamic matching against incoming request origins.
 *
 * <p>Supports wildcard origin matching (e.g., {@code http://*.example.com}) by compiling patterns into regex.
 * The cache is automatically refreshed from the database on a scheduled interval.
 */
@Service
@RequiredArgsConstructor
public class CorsPolicyService {

    private static final Logger log = LoggerFactory.getLogger(CorsPolicyService.class);

    private final CorsPolicyRepository repository;

    /** In-memory cache of compiled policies. Declared volatile for thread-safe swaps. */
    private volatile List<CompiledCorsPolicy> cachedPolicies;

    @PostConstruct
    @Scheduled(fixedRateString = "${security.cors.cache-refresh-ms:60000}")
    public void loadPolicies() {
        log.info("Refreshing active CORS policies from database...");
        List<CorsPolicy> activePolicies = repository.findAllByIsActiveTrue();
        
        List<CompiledCorsPolicy> compiledList = new ArrayList<>();
        for (CorsPolicy policy : activePolicies) {
            try {
                Pattern pattern = globToRegex(policy.getAllowedOrigin());
                compiledList.add(new CompiledCorsPolicy(policy, pattern));
            } catch (Exception e) {
                log.error("Failed to compile CORS policy origin pattern '{}' (ID: {}): {}", 
                        policy.getAllowedOrigin(), policy.getId(), e.getMessage());
            }
        }
        
        this.cachedPolicies = compiledList;
        log.info("Loaded {} active CORS policy configurations", compiledList.size());
    }

    /**
     * Force immediate cache reload (e.g. after CRUD operations).
     */
    public void forceReload() {
        log.info("Force-reloading CORS policy configurations from database");
        loadPolicies();
    }

    /**
     * Checks if the incoming request origin is allowed by any active CORS policies.
     *
     * @param origin The request origin (from HTTP header).
     * @return Match result containing the built CorsConfiguration if successful.
     */
    public CorsPolicyMatchingResult getMatchingPolicy(String origin) {
        if (origin == null) {
            return CorsPolicyMatchingResult.unmatched();
        }

        List<CompiledCorsPolicy> active = this.cachedPolicies;
        if (active == null) {
            ensureLoaded();
            active = this.cachedPolicies;
        }

        for (CompiledCorsPolicy compiled : active) {
            if (compiled.getOriginPattern().matcher(origin).matches()) {
                log.debug("CORS request origin '{}' allowed by policy ID: {}", origin, compiled.getPolicy().getId());
                CorsConfiguration config = buildCorsConfiguration(compiled.getPolicy(), origin);
                return CorsPolicyMatchingResult.matched(config);
            }
        }

        log.warn("CORS request rejected: Origin '{}' is not allowed by any active CORS policies.", origin);
        return CorsPolicyMatchingResult.unmatched();
    }

    private synchronized void ensureLoaded() {
        if (cachedPolicies == null) {
            loadPolicies();
        }
    }

    /**
     * Map a CorsPolicy database entity to a Spring CorsConfiguration object.
     */
    private CorsConfiguration buildCorsConfiguration(CorsPolicy policy, String requestOrigin) {
        CorsConfiguration config = new CorsConfiguration();

        // Safety: If credentials are allowed, we MUST specify the exact request origin
        // in Access-Control-Allow-Origin, rather than wildcard (*), which browsers reject.
        if (Boolean.TRUE.equals(policy.getAllowCredentials())) {
            config.setAllowedOrigins(List.of(requestOrigin));
            config.setAllowCredentials(true);
        } else {
            config.setAllowedOrigins(List.of(policy.getAllowedOrigin()));
            config.setAllowCredentials(false);
        }

        // Map allowed methods
        if (policy.getAllowedMethods() != null && !policy.getAllowedMethods().isBlank()) {
            config.setAllowedMethods(Arrays.stream(policy.getAllowedMethods().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }

        // Map allowed headers
        if (policy.getAllowedHeaders() != null && !policy.getAllowedHeaders().isBlank()) {
            config.setAllowedHeaders(Arrays.stream(policy.getAllowedHeaders().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }

        // Map exposed headers
        if (policy.getExposedHeaders() != null && !policy.getExposedHeaders().isBlank()) {
            config.setExposedHeaders(Arrays.stream(policy.getExposedHeaders().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }

        if (policy.getMaxAge() != null) {
            config.setMaxAge(policy.getMaxAge());
        }

        return config;
    }

    /**
     * Convert a glob-style origin pattern (e.g., http://*.domain.com) into a regex pattern.
     */
    private static Pattern globToRegex(String glob) {
        if ("*".equals(glob)) {
            return Pattern.compile("^.*$");
        }
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*') {
                sb.append("[a-zA-Z0-9.-]*");
            } else if ("./\\?+^$[]{}()|".indexOf(c) != -1) {
                sb.append('\\').append(c);
            } else {
                sb.append(c);
            }
        }
        sb.append("$");
        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Helper holder class for a cached policy and its compiled regex pattern.
     */
    @Getter
    @RequiredArgsConstructor
    private static class CompiledCorsPolicy {
        private final CorsPolicy policy;
        private final Pattern originPattern;
    }
}
