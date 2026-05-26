package org.tradebook.journal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.tradebook.journal.features.cors.entity.CorsPolicy;
import org.tradebook.journal.features.cors.repository.CorsPolicyRepository;
import org.tradebook.journal.features.cors.service.CorsPolicyService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the dynamic, cached CORS policy management system.
 * Verifies that requests with registered origins (and subdomains matching wildcard rules)
 * receive appropriate CORS response headers, while unconfigured origins are rejected.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorsPolicyRepository corsPolicyRepository;

    @Autowired
    private CorsPolicyService corsPolicyService;

    /**
     * Verifies that requests from seeded development origins (like http://localhost:4200)
     * succeed and receive correct CORS headers.
     */
    @Test
    public void testAllowedSeededOrigin_Success() throws Exception {
        mockMvc.perform(options("/api/v1/trades")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    /**
     * Verifies that requests from unregistered/disallowed origins do not receive CORS headers.
     */
    @Test
    public void testDisallowedOrigin_NoCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/trades")
                .header(HttpHeaders.ORIGIN, "http://malicious-origin.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * Verifies that wildcard patterns (e.g. http://*.example.com) allow subdomain matches
     * and dynamically echo the correct request origin in the Access-Control-Allow-Origin response header.
     */
    @Test
    public void testDynamicWildcardOrigin_Success() throws Exception {
        // Temporarily insert a wildcard policy in the DB
        CorsPolicy wildcardPolicy = CorsPolicy.builder()
                .allowedOrigin("http://*.example.com")
                .allowedMethods("GET,POST,PUT,DELETE,OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600L)
                .isActive(true)
                .description("Test wildcard domain policy")
                .build();

        CorsPolicy saved = corsPolicyRepository.save(wildcardPolicy);
        try {
            // Force cache refresh immediately
            corsPolicyService.forceReload();

            // Perform OPTIONS preflight from a matching subdomain
            mockMvc.perform(options("/api/v1/trades")
                    .header(HttpHeaders.ORIGIN, "http://app.example.com")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://app.example.com"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));

            // Perform OPTIONS preflight from a non-matching subdomain / domain
            mockMvc.perform(options("/api/v1/trades")
                    .header(HttpHeaders.ORIGIN, "http://example.com")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        } finally {
            // Clean up DB and cache
            corsPolicyRepository.delete(saved);
            corsPolicyService.forceReload();
        }
    }
}
