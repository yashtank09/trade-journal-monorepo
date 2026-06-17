package org.tradebook.journal.features.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.features.auth.entity.SecurityEndpointConfig;
import org.tradebook.journal.features.auth.enums.AccessLevel;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityEndpointConfigRepository extends JpaRepository<SecurityEndpointConfig, Long> {

    /** Used by the caching service to fetch only enabled rows. */
    List<SecurityEndpointConfig> findAllByIsActiveTrue();

    /** Used by the caching service to filter by level. */
    List<SecurityEndpointConfig> findAllByAccessLevelAndIsActiveTrue(AccessLevel accessLevel);

    Optional<SecurityEndpointConfig> findByPath(String path);
}
