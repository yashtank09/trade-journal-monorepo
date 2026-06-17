package org.tradebook.journal.features.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.tradebook.journal.config.ApplicationProperties;
import org.tradebook.journal.features.email.service.EmailQueueService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final EmailQueueService emailQueueService;
    private final TemplateEngine templateEngine;
    private final ApplicationProperties applicationProperties;

    /**
     * Sends a password reset email with a link containing the reset token.
     *
     * @param toEmail    the recipient's email address
     * @param resetToken the UUID reset token
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = applicationProperties.getFrontendUrl() + "/auth/reset-password?token=" + resetToken;
        int expiryMinutes = applicationProperties.getPasswordResetTokenExpiryMinutes();

        Context context = new Context();
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("expiryMinutes", expiryMinutes);

        String htmlContent = templateEngine.process("password-reset-email", context);

        try {
            emailQueueService.queueEmail(toEmail, "TradeJournal — Password Reset Request", htmlContent);
            log.info("Password reset email queued for: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to queue password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to queue password reset email", e);
        }
    }
}
