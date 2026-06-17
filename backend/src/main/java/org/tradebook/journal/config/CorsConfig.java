package org.tradebook.journal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.tradebook.journal.features.cors.dto.CorsPolicyMatchingResult;
import org.tradebook.journal.features.cors.service.CorsPolicyService;

/**
 * Configuration class exposing the custom CORS policy registry for the application.
 * Exposes a {@link CorsConfigurationSource} which dynamic-checks origins against active DB rules.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsPolicyService corsPolicyService;

    /**
     * Exposes the CORS configuration source bean used by both Spring Web and Spring Security.
     * Evaluates incoming CORS origins against registered database rules in real-time.
     *
     * @return The dynamic CORS configuration source.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            String origin = request.getHeader("Origin");
            if (origin == null) {
                // Not a CORS request (e.g. direct curl or same-domain client request)
                return null;
            }
            
            // Query dynamic cache for matching policy rules
            CorsPolicyMatchingResult result = corsPolicyService.getMatchingPolicy(origin);
            if (result.isMatched()) {
                return result.getConfiguration();
            }
            
            // Return null if no matching origin is registered.
            // Under Spring CORS execution, returning null prevents CORS response headers from being added,
            // which causes the client browser to reject the cross-origin request.
            return null;
        };
    }
}
