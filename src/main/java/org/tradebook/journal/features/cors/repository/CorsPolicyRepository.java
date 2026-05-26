package org.tradebook.journal.features.cors.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.features.cors.entity.CorsPolicy;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CorsPolicy} instances in the database.
 */
@Repository
public interface CorsPolicyRepository extends JpaRepository<CorsPolicy, Long> {

    /**
     * Retrieve all active CORS policies.
     *
     * @return List of active CORS policies.
     */
    List<CorsPolicy> findAllByIsActiveTrue();

    /**
     * Find a CORS policy by its unique allowed origin.
     *
     * @param allowedOrigin The allowed origin to search for.
     * @return An Optional containing the policy if found.
     */
    Optional<CorsPolicy> findByAllowedOrigin(String allowedOrigin);

    /**
     * Check if a CORS policy already exists for the given origin.
     *
     * @param allowedOrigin The allowed origin to check.
     * @return True if a policy exists, false otherwise.
     */
    boolean existsByAllowedOrigin(String allowedOrigin);
}
