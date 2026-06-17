package org.tradebook.journal.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tradebook.journal.features.auth.service.SecurityEndpointConfigService;
import org.tradebook.journal.features.auth.service.TokenBlacklistService;

import java.io.IOException;

/**
 * JWT authentication filter.
 *
 * <p>Public paths (as defined in {@link SecurityEndpointConfigService}) are
 * automatically skipped via {@link #shouldNotFilter}, matching the pattern
 * used in the Sievex-Application reference implementation.  For all other
 * requests the filter validates the {@code Authorization: Bearer <token>}
 * header and populates the {@link SecurityContextHolder}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecurityEndpointConfigService securityEndpointConfigService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // -------------------------------------------------------------------------
    // Skip public paths – no JWT validation needed
    // -------------------------------------------------------------------------

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return securityEndpointConfigService.getPublicPaths()
                .stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    // -------------------------------------------------------------------------
    // Core JWT validation
    // -------------------------------------------------------------------------

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt       = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails) && !tokenBlacklistService.isBlacklisted(jwt)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
