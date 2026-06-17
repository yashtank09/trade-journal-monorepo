package org.tradebook.journal.features.auth.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradebook.journal.common.dto.DataApiResponse;
import org.tradebook.journal.features.auth.dto.request.UpdateProfileRequest;
import org.tradebook.journal.features.auth.dto.response.UserProfileResponse;
import org.tradebook.journal.features.auth.service.AuthService;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<DataApiResponse<UserProfileResponse>> getProfile(@Parameter(hidden = true) Principal principal) {
        String email = principal.getName();
        UserProfileResponse response = authService.getUserProfile(email);
        return ResponseEntity.ok(DataApiResponse.success(200, "Profile retrieved successfully", response));
    }

    @PutMapping("/profile")
    public ResponseEntity<DataApiResponse<UserProfileResponse>> updateProfile(
            @Parameter(hidden = true) Principal principal,
            @RequestBody UpdateProfileRequest request) {
        String email = principal.getName();
        UserProfileResponse response = authService.updateUserProfile(email, request);
        return ResponseEntity.ok(DataApiResponse.success(200, "Profile updated successfully", response));
    }
}
