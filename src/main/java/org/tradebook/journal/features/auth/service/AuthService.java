package org.tradebook.journal.features.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tradebook.journal.common.exception.EmailAlreadyExistsException;
import org.tradebook.journal.config.ApplicationProperties;
import org.tradebook.journal.config.security.JwtService;
import org.tradebook.journal.features.auth.dto.response.AuthResponse;
import org.tradebook.journal.features.auth.dto.request.LoginRequest;
import org.tradebook.journal.features.auth.dto.request.RegisterRequest;
import org.tradebook.journal.features.auth.entity.User;
import org.tradebook.journal.features.auth.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

import static org.tradebook.journal.features.auth.AuthConstants.MSG_EMAIL_IN_USE;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ApplicationProperties applicationProperties;

    public AuthResponse register(RegisterRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(MSG_EMAIL_IN_USE);
        }

        var user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .currency(request.getCurrency() != null ? request.getCurrency() : applicationProperties.getDefaultCurrency())
                .build();
        repository.save(user);

        // Generate token for immediate login
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ROLE_" + user.getRole().name());
        var jwtToken = jwtService.generateToken(extraClaims, userDetails);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));
        var userDetails = userDetailsService.loadUserByUsername(request.getUserName());
        
        var user = repository.findByEmail(request.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUserName()));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ROLE_" + user.getRole().name());
        var jwtToken = jwtService.generateToken(extraClaims, userDetails);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public void logout(String token) {
        tokenBlacklistService.blacklist(token);
    }
}
