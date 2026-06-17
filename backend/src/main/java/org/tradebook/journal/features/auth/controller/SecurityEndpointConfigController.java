package org.tradebook.journal.features.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradebook.journal.common.dto.DataApiResponse;
import org.tradebook.journal.features.auth.dto.request.SecurityEndpointConfigRequest;
import org.tradebook.journal.features.auth.dto.response.SecurityEndpointConfigResponse;
import org.tradebook.journal.features.auth.service.SecurityEndpointConfigCrudService;
import org.tradebook.journal.features.auth.service.SecurityEndpointConfigService;

import java.util.List;

import static org.tradebook.journal.common.constants.ApiConstants.CODE_SUCCESS;

/**
 * Admin-only REST API for managing dynamic security endpoint configurations.
 *
 * <p>Base path: {@code /admin/security-configs}
 *
 * <p>All endpoints under {@code /admin/**} should be mapped to the {@code ADMIN}
 * access level in the {@code security_endpoint_configs} table.
 */
@RestController
@RequestMapping("/admin/security-configs")
@RequiredArgsConstructor
public class SecurityEndpointConfigController {

    private final SecurityEndpointConfigCrudService crudService;
    private final SecurityEndpointConfigService configService;

    /** List all configured endpoint rules (including inactive). */
    @GetMapping
    public ResponseEntity<DataApiResponse<List<SecurityEndpointConfigResponse>>> getAll() {
        List<SecurityEndpointConfigResponse> configs = crudService.getAll();
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "Fetched all security configs", configs));
    }

    /** Get a single config by its database ID. */
    @GetMapping("/{id}")
    public ResponseEntity<DataApiResponse<SecurityEndpointConfigResponse>> getById(@PathVariable Long id) {
        SecurityEndpointConfigResponse cfg = crudService.getById(id);
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "Fetched security config", cfg));
    }

    /** Create a new endpoint-access rule. */
    @PostMapping
    public ResponseEntity<DataApiResponse<SecurityEndpointConfigResponse>> create(
            @Valid @RequestBody SecurityEndpointConfigRequest request) {
        SecurityEndpointConfigResponse created = crudService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DataApiResponse.success(CODE_SUCCESS, "Security config created", created));
    }

    /** Update an existing rule. */
    @PutMapping("/{id}")
    public ResponseEntity<DataApiResponse<SecurityEndpointConfigResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SecurityEndpointConfigRequest request) {
        SecurityEndpointConfigResponse updated = crudService.update(id, request);
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "Security config updated", updated));
    }

    /** Toggle the {@code isActive} flag without deleting. */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<DataApiResponse<SecurityEndpointConfigResponse>> toggle(@PathVariable Long id) {
        SecurityEndpointConfigResponse toggled = crudService.toggleActive(id);
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "Security config toggled", toggled));
    }

    /** Delete a rule permanently. */
    @DeleteMapping("/{id}")
    public ResponseEntity<DataApiResponse<Void>> delete(@PathVariable Long id) {
        crudService.delete(id);
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "Security config deleted", null));
    }

    /**
     * Force an immediate refresh of the in-memory path cache from the database.
     * Useful after bulk SQL edits or after restarting if the cache is stale.
     */
    @PostMapping("/reload")
    public ResponseEntity<DataApiResponse<Void>> reload() {
        configService.forceReload();
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "Security config cache reloaded", null));
    }
}
