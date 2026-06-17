package org.tradebook.journal.features.cors.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradebook.journal.common.dto.DataApiResponse;
import org.tradebook.journal.features.cors.dto.request.CorsPolicyRequest;
import org.tradebook.journal.features.cors.dto.response.CorsPolicyResponse;
import org.tradebook.journal.features.cors.service.CorsPolicyCrudService;
import org.tradebook.journal.features.cors.service.CorsPolicyService;

import java.util.List;

import static org.tradebook.journal.common.constants.ApiConstants.CODE_SUCCESS;

/**
 * Admin-only REST controller for managing dynamic CORS policies.
 *
 * <p>Base path: {@code /admin/cors-policies}
 * Access to endpoints matching {@code /admin/**} requires administrative privileges (ADMIN role).
 */
@RestController
@RequestMapping("/admin/cors-policies")
@RequiredArgsConstructor
public class CorsPolicyController {

    private final CorsPolicyCrudService crudService;
    private final CorsPolicyService corsPolicyService;

    /**
     * Get a list of all configured CORS policies.
     */
    @GetMapping
    public ResponseEntity<DataApiResponse<List<CorsPolicyResponse>>> getAll() {
        List<CorsPolicyResponse> policies = crudService.getAll();
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "Fetched all CORS policies", policies));
    }

    /**
     * Get details of a single CORS policy.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataApiResponse<CorsPolicyResponse>> getById(@PathVariable Long id) {
        CorsPolicyResponse policy = crudService.getById(id);
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "Fetched CORS policy", policy));
    }

    /**
     * Create a new CORS policy.
     */
    @PostMapping
    public ResponseEntity<DataApiResponse<CorsPolicyResponse>> create(
            @Valid @RequestBody CorsPolicyRequest request) {
        CorsPolicyResponse created = crudService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DataApiResponse.success(CODE_SUCCESS, "CORS policy created successfully", created));
    }

    /**
     * Update an existing CORS policy.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DataApiResponse<CorsPolicyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CorsPolicyRequest request) {
        CorsPolicyResponse updated = crudService.update(id, request);
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "CORS policy updated successfully", updated));
    }

    /**
     * Toggle the active status of a CORS policy.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<DataApiResponse<CorsPolicyResponse>> toggle(@PathVariable Long id) {
        CorsPolicyResponse toggled = crudService.toggleActive(id);
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "CORS policy active status toggled", toggled));
    }

    /**
     * Delete a CORS policy.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DataApiResponse<Void>> delete(@PathVariable Long id) {
        crudService.delete(id);
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "CORS policy deleted successfully", null));
    }

    /**
     * Force immediate cache refresh of the in-memory CORS policy cache from the database.
     */
    @PostMapping("/reload")
    public ResponseEntity<DataApiResponse<Void>> reload() {
        corsPolicyService.forceReload();
        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, "CORS policy cache reloaded successfully", null));
    }
}
