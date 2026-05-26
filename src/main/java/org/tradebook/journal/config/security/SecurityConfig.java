package org.tradebook.journal.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.tradebook.journal.features.auth.service.SecurityEndpointConfigService;

import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 *
 * <p>Endpoint authorization rules are <strong>not</strong> hard-coded here.
 * They are loaded at startup from the {@code security_endpoint_configs} database
 * table via {@link SecurityEndpointConfigService}, which also refreshes them
 * every 60 seconds so runtime changes propagate without a restart.
 *
 * <p>The filter chain is still built once at application startup (Spring Security
 * requirement), but the cached lists are stable enough for day-to-day use.
 * For immediate effect of a DB change, call {@code POST /admin/security-config/reload}.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final SecurityEndpointConfigService securityEndpointConfigService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        List<String> publicPaths  = securityEndpointConfigService.getPublicPaths();
        List<String> userPaths    = securityEndpointConfigService.getUserPaths();
        List<String> adminPaths   = securityEndpointConfigService.getAdminPaths();

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {

                    // 1. Public paths – no token required
                    if (!publicPaths.isEmpty()) {
                        auth.requestMatchers(publicPaths.toArray(new String[0])).permitAll();
                    }

                    // 2. Admin-only paths
                    if (!adminPaths.isEmpty()) {
                        auth.requestMatchers(adminPaths.toArray(new String[0])).hasRole("ADMIN");
                    }

                    // 3. Regular authenticated paths
                    if (!userPaths.isEmpty()) {
                        auth.requestMatchers(userPaths.toArray(new String[0])).authenticated();
                    }

                    // 4. Everything else also requires authentication
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
