package org.tradebook.journal.features.auth.enums;

/**
 * Defines the access level required to reach a given API endpoint.
 * These levels map directly to Spring Security's request-matcher
 * rules in {@code SecurityConfig} and are stored in the database,
 * allowing endpoint access rules to be changed at runtime without
 * redeploying the application.
 */
public enum AccessLevel {

    /**
     * No authentication required – typically auth/login/register endpoints
     * and Swagger/actuator health paths.
     */
    PUBLIC,

    /**
     * Any authenticated user can access these endpoints.
     */
    USER,

    /**
     * Only users that hold the ADMIN role can access these endpoints.
     */
    ADMIN
}
