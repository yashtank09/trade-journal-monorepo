package org.tradebook.journal.features.cors.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Result object returned when matching a request origin against active CORS policies.
 */
@Getter
@RequiredArgsConstructor
public class CorsPolicyMatchingResult {

    private final boolean matched;
    private final CorsConfiguration configuration;

    /**
     * Create an unmatched result instance.
     *
     * @return A result indicating no policy matched.
     */
    public static CorsPolicyMatchingResult unmatched() {
        return new CorsPolicyMatchingResult(false, null);
    }

    /**
     * Create a matched result instance wrapping the matched configuration.
     *
     * @param configuration The built Spring CorsConfiguration.
     * @return A result indicating a policy matched.
     */
    public static CorsPolicyMatchingResult matched(CorsConfiguration configuration) {
        return new CorsPolicyMatchingResult(true, configuration);
    }
}
