package org.tradebook.journal.features.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradebook.journal.common.dto.DataApiResponse;
import org.tradebook.journal.features.auth.dto.response.AuthResponse;
import org.tradebook.journal.features.auth.dto.request.LoginRequest;
import org.tradebook.journal.features.auth.dto.request.RegisterRequest;
import org.tradebook.journal.features.auth.service.AuthService;

import static org.tradebook.journal.common.constants.ApiConstants.*;
import static org.tradebook.journal.features.auth.AuthConstants.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<DataApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        AuthResponse response = service.register(request);
        return ResponseEntity.ok(new DataApiResponse<>(STATUS_SUCCESS, CODE_SUCCESS, MSG_REGISTER_SUCCESS, response.getToken(), response));
    }

    @PostMapping("/login")
    public ResponseEntity<DataApiResponse<AuthResponse>> authenticate(@RequestBody LoginRequest request) {
        AuthResponse response = service.authenticate(request);
        return ResponseEntity.ok(new DataApiResponse<>(STATUS_SUCCESS, CODE_SUCCESS, MSG_LOGIN_SUCCESS, response.getToken(), response));
    }

    @PostMapping("/logout")
    public ResponseEntity<DataApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            service.logout(token);
        }
        return ResponseEntity.ok(new DataApiResponse<>(STATUS_SUCCESS, CODE_SUCCESS, MSG_LOGOUT_SUCCESS, null, null));
    }
}
