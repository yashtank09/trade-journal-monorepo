package org.tradebook.journal.features.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tradebook.journal.config.ApplicationProperties;
import org.tradebook.journal.features.auth.entity.PasswordResetToken;
import org.tradebook.journal.features.auth.entity.User;
import org.tradebook.journal.features.auth.repository.PasswordResetTokenRepository;
import org.tradebook.journal.features.auth.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.tradebook.journal.features.auth.AuthConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ApplicationProperties applicationProperties;

    /**
     * Initiates a password reset flow.
     * Always returns successfully to prevent email enumeration attacks.
     *
     * @param email the user's email address
     */
    @Transactional
    public void requestPasswordReset(String email) {
        var userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Silently return — don't reveal whether the email is registered
            log.debug("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOptional.get();

        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        int expiryMinutes = applicationProperties.getPasswordResetTokenExpiryMinutes();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();

        tokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {}", user.getEmail());

        // Send the email
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    /**
     * Validates the reset token and updates the user's password.
     *
     * @param token       the reset token from the email link
     * @param newPassword the new plain-text password
     * @throws IllegalArgumentException if the token is invalid, expired, or already used
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException(MSG_INVALID_RESET_TOKEN));

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException(MSG_INVALID_RESET_TOKEN);
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException(MSG_EXPIRED_RESET_TOKEN);
        }

        // Update user's password
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getEmail());
    }
}
