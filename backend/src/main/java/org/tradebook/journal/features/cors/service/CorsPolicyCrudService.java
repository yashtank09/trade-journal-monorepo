package org.tradebook.journal.features.cors.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tradebook.journal.features.cors.dto.request.CorsPolicyRequest;
import org.tradebook.journal.features.cors.dto.response.CorsPolicyResponse;
import org.tradebook.journal.features.cors.entity.CorsPolicy;
import org.tradebook.journal.features.cors.repository.CorsPolicyRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service providing database CRUD operations for {@link CorsPolicy} entities.
 * Automatically triggers cache invalidation/refresh on any modifications.
 */
@Service
@RequiredArgsConstructor
public class CorsPolicyCrudService {

    private static final Logger log = LoggerFactory.getLogger(CorsPolicyCrudService.class);

    private final CorsPolicyRepository repository;
    private final CorsPolicyService corsPolicyService;

    /**
     * Retrieve all CORS policies (both active and inactive).
     */
    @Transactional(readOnly = true)
    public List<CorsPolicyResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single CORS policy by database ID.
     */
    @Transactional(readOnly = true)
    public CorsPolicyResponse getById(Long id) {
        CorsPolicy entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CORS policy not found with id: " + id));
        return toResponse(entity);
    }

    /**
     * Create a new CORS policy, validate its fields, and refresh the CORS cache.
     */
    @Transactional
    public CorsPolicyResponse create(CorsPolicyRequest request) {
        validateRequest(request, null);

        CorsPolicy entity = CorsPolicy.builder()
                .allowedOrigin(request.getAllowedOrigin().trim())
                .allowedMethods(request.getAllowedMethods().trim())
                .allowedHeaders(request.getAllowedHeaders().trim())
                .exposedHeaders(request.getExposedHeaders() != null ? request.getExposedHeaders().trim() : null)
                .allowCredentials(request.getAllowCredentials())
                .maxAge(request.getMaxAge())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .description(request.getDescription())
                .build();

        CorsPolicy saved = repository.save(entity);
        log.info("Admin created new CORS policy: ID={}, Origin='{}'", saved.getId(), saved.getAllowedOrigin());
        
        // Push update to in-memory cache
        corsPolicyService.forceReload();

        return toResponse(saved);
    }

    /**
     * Update an existing CORS policy, validate its fields, and refresh the CORS cache.
     */
    @Transactional
    public CorsPolicyResponse update(Long id, CorsPolicyRequest request) {
        CorsPolicy entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CORS policy not found with id: " + id));

        validateRequest(request, id);

        entity.setAllowedOrigin(request.getAllowedOrigin().trim());
        entity.setAllowedMethods(request.getAllowedMethods().trim());
        entity.setAllowedHeaders(request.getAllowedHeaders().trim());
        entity.setExposedHeaders(request.getExposedHeaders() != null ? request.getExposedHeaders().trim() : null);
        entity.setAllowCredentials(request.getAllowCredentials());
        entity.setMaxAge(request.getMaxAge());
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
        entity.setDescription(request.getDescription());

        CorsPolicy updated = repository.save(entity);
        log.info("Admin updated CORS policy: ID={}, Origin='{}'", updated.getId(), updated.getAllowedOrigin());

        // Push update to in-memory cache
        corsPolicyService.forceReload();

        return toResponse(updated);
    }

    /**
     * Toggle the active status of a CORS policy and refresh the CORS cache.
     */
    @Transactional
    public CorsPolicyResponse toggleActive(Long id) {
        CorsPolicy entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CORS policy not found with id: " + id));

        entity.setIsActive(!entity.getIsActive());
        CorsPolicy saved = repository.save(entity);
        log.info("Admin toggled CORS policy active status: ID={}, Origin='{}', Active={}", 
                saved.getId(), saved.getAllowedOrigin(), saved.getIsActive());

        // Push update to in-memory cache
        corsPolicyService.forceReload();

        return toResponse(saved);
    }

    /**
     * Permanently delete a CORS policy and refresh the CORS cache.
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("CORS policy not found with id: " + id);
        }
        repository.deleteById(id);
        log.info("Admin deleted CORS policy: ID={}", id);

        // Push update to in-memory cache
        corsPolicyService.forceReload();
    }

    /**
     * Validates safety constraints and duplicate checks.
     */
    private void validateRequest(CorsPolicyRequest request, Long existingId) {
        String origin = request.getAllowedOrigin().trim();

        // 1. Safety Constraint: Do not allow '*' with credentials enabled
        if (Boolean.TRUE.equals(request.getAllowCredentials()) && "*".equals(origin)) {
            throw new IllegalArgumentException("Wildcard '*' allowed origin cannot be used when allowCredentials is enabled for security reasons.");
        }

        // 2. Format validation: Must be '*' or start with http:// or https://
        if (!"*".equals(origin) && !origin.startsWith("http://") && !origin.startsWith("https://")) {
            throw new IllegalArgumentException("Allowed origin must be a wildcard '*' or begin with 'http://' or 'https://'");
        }

        // 3. Prevent duplicate allowed origin settings
        repository.findByAllowedOrigin(origin).ifPresent(existing -> {
            if (existingId == null || !existing.getId().equals(existingId)) {
                throw new IllegalArgumentException("A CORS policy for origin '" + origin + "' already exists.");
            }
        });
    }

    /**
     * Map entity to response DTO.
     */
    private CorsPolicyResponse toResponse(CorsPolicy entity) {
        return CorsPolicyResponse.builder()
                .id(entity.getId())
                .allowedOrigin(entity.getAllowedOrigin())
                .allowedMethods(entity.getAllowedMethods())
                .allowedHeaders(entity.getAllowedHeaders())
                .exposedHeaders(entity.getExposedHeaders())
                .allowCredentials(entity.getAllowCredentials())
                .maxAge(entity.getMaxAge())
                .isActive(entity.getIsActive())
                .description(entity.getDescription())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
