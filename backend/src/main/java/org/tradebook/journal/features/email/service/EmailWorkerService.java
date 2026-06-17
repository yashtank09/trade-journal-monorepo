package org.tradebook.journal.features.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tradebook.journal.features.email.entity.EmailOutbox;
import org.tradebook.journal.features.email.repository.EmailOutboxRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailWorkerService {

    private final EmailOutboxRepository outboxRepository;
    private final DynamicMailSenderProvider mailSenderProvider;
    private final org.tradebook.journal.features.sysconfig.service.ConfigurationService configurationService;

    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 10000) // Run every 10 seconds
    @Transactional
    public void processOutboxEmails() {
        // Fetch up to 50 PENDING emails
        List<EmailOutbox> pendingEmails = outboxRepository.findByStatusOrderByCreatedAtAsc(
                "PENDING", PageRequest.of(0, 50));

        if (pendingEmails.isEmpty()) {
            return;
        }

        log.info("Processing {} pending emails from outbox.", pendingEmails.size());
        
        // Get the latest mail sender
        JavaMailSender mailSender = mailSenderProvider.getMailSender();

        for (EmailOutbox email : pendingEmails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                String fromEmail = configurationService.getConfigValue("MAIL_FROM", configurationService.getConfigValue("MAIL_USERNAME", "noreply@localhost"));
                helper.setFrom(fromEmail);
                
                String toEmailConfig = configurationService.getConfigValue("MAIL_TO", "");
                if (toEmailConfig != null && !toEmailConfig.trim().isEmpty()) {
                    helper.setTo(toEmailConfig);
                    log.debug("Overriding TO address with configured MAIL_TO: {}", toEmailConfig);
                } else {
                    helper.setTo(email.getRecipientEmail());
                }
                
                helper.setSubject(email.getSubject());
                helper.setText(email.getBody(), true);
                
                mailSender.send(message);
                
                // Mark as sent
                email.setStatus("SENT");
                email.setSentAt(LocalDateTime.now());
                log.info("Successfully sent email to {}", email.getRecipientEmail());
                
            } catch (Exception e) {
                log.error("Failed to send email to {}", email.getRecipientEmail(), e);
                email.setRetryCount(email.getRetryCount() + 1);
                email.setErrorMessage(e.getMessage());
                
                if (email.getRetryCount() >= MAX_RETRIES) {
                    email.setStatus("FAILED");
                    log.error("Email to {} reached max retries and marked as FAILED.", email.getRecipientEmail());
                }
            }
        }
        
        // The @Transactional annotation ensures all status updates are saved at the end of the method
        outboxRepository.saveAll(pendingEmails);
    }
}
