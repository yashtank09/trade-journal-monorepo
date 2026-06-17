package org.tradebook.journal.features.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tradebook.journal.features.auth.dto.request.SecurityEndpointConfigRequest;
import org.tradebook.journal.features.auth.dto.response.SecurityEndpointConfigResponse;
import org.tradebook.journal.features.auth.entity.SecurityEndpointConfig;
import org.tradebook.journal.features.auth.repository.SecurityEndpointConfigRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * CRUD operations for {@link SecurityEndpointConfig} rows.
 *
 * <p>After every mutating operation this service calls
 * {@link SecurityEndpointConfigService#forceReload()} so the JWT filter and
 * security config pick up the change within the same request cycle.
 */
@Service
@RequiredArgsConstructor
public class SecurityEndpointConfigCrudService {

    private final SecurityEndpointConfigRepository repository;
    private final SecurityEndpointConfigService configService;

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SecurityEndpointConfigResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SecurityEndpointConfigResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Security config not found with id: " + id));
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    public SecurityEndpointConfigResponse create(SecurityEndpointConfigRequest request) {
        repository.findByPath(request.getPath()).ifPresent(existing -> {
            throw new IllegalArgumentException("A config for path '" + request.getPath() + "' already exists (id=" + existing.getId() + ")");
        });

        SecurityEndpointConfig entity = SecurityEndpointConfig.builder()
                .path(request.getPath())
                .accessLevel(request.getAccessLevel())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .description(request.getDescription())
                .build();

        SecurityEndpointConfig saved = repository.save(entity);
        configService.forceReload();
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Transactional
    public SecurityEndpointConfigResponse update(Long id, SecurityEndpointConfigRequest request) {
        SecurityEndpointConfig entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Security config not found with id: " + id));

        // Check path uniqueness if path is being changed
        if (!entity.getPath().equals(request.getPath())) {
            repository.findByPath(request.getPath()).ifPresent(conflict -> {
                throw new IllegalArgumentException("Another config already uses path '" + request.getPath() + "' (id=" + conflict.getId() + ")");
            });
        }

        entity.setPath(request.getPath());
        entity.setAccessLevel(request.getAccessLevel());
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
        entity.setDescription(request.getDescription());

        SecurityEndpointConfig updated = repository.save(entity);
        configService.forceReload();
        return toResponse(updated);
    }

    // -------------------------------------------------------------------------
    // Delete / Toggle
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Security config not found with id: " + id);
        }
        repository.deleteById(id);
        configService.forceReload();
    }

    @Transactional
    public SecurityEndpointConfigResponse toggleActive(Long id) {
        SecurityEndpointConfig entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Security config not found with id: " + id));
        entity.setIsActive(!entity.getIsActive());
        SecurityEndpointConfig saved = repository.save(entity);
        configService.forceReload();
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private SecurityEndpointConfigResponse toResponse(SecurityEndpointConfig entity) {
        return SecurityEndpointConfigResponse.builder()
                .id(entity.getId())
                .path(entity.getPath())
                .accessLevel(entity.getAccessLevel())
                .isActive(entity.getIsActive())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
