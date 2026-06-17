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

import org.tradebook.journal.common.exception.UsernameAlreadyExistsException;
import org.tradebook.journal.common.exception.TradeBookException;
import org.tradebook.journal.features.auth.dto.request.UpdateProfileRequest;
import org.tradebook.journal.features.auth.dto.response.UserProfileResponse;

import static org.tradebook.journal.features.auth.AuthConstants.MSG_EMAIL_IN_USE;
import static org.tradebook.journal.features.auth.AuthConstants.MSG_USERNAME_IN_USE;

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
        
        if (request.getUsername() != null && repository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(MSG_USERNAME_IN_USE);
        }

        var user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
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
        // Resolve the user first to determine if they entered an email or username
        var user = repository.findByEmailOrUsername(request.getUserName(), request.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUserName()));

        // Authenticate using the resolved email (since our UserDetailsService uses email)
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword()));
        
        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ROLE_" + user.getRole().name());
        var jwtToken = jwtService.generateToken(extraClaims, userDetails);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public void logout(String token) {
        tokenBlacklistService.blacklist(token);
    }

    public UserProfileResponse getUserProfile(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new TradeBookException("User not found: " + email));
        
        String joinedMonthYear = "";
        if (user.getCreatedAt() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy");
            joinedMonthYear = user.getCreatedAt().format(formatter);
        }

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobileNumber(user.getMobileNumber())
                .residentialAddress(user.getResidentialAddress())
                .pan(user.getPan())
                .currency(user.getCurrency())
                .tradingStyle(user.getTradingStyle())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .joinedDate(joinedMonthYear)
                .build();
    }

    public UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new TradeBookException("User not found: " + email));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMobileNumber(request.getMobileNumber());
        user.setResidentialAddress(request.getResidentialAddress());
        user.setPan(request.getPan() != null ? request.getPan().toUpperCase() : null);
        user.setCurrency(request.getCurrency());
        user.setTradingStyle(request.getTradingStyle());

        repository.save(user);

        return getUserProfile(email);
    }
}
