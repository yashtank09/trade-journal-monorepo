package org.tradebook.journal.features.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tradebook.journal.features.email.entity.EmailOutbox;
import org.tradebook.journal.features.email.repository.EmailOutboxRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {

    private final EmailOutboxRepository repository;

    /**
     * Queues an email to be sent asynchronously by the outbox worker.
     * This method should be called within the same transaction as the business logic
     * so that if the business logic rolls back, the email is not sent.
     */
    @Transactional
    public void queueEmail(String recipient, String subject, String htmlBody) {
        EmailOutbox outbox = EmailOutbox.builder()
                .recipientEmail(recipient)
                .subject(subject)
                .body(htmlBody)
                .status("PENDING")
                .retryCount(0)
                .build();
                
        repository.save(outbox);
        log.info("Queued email for recipient: {}, subject: {}", recipient, subject);
    }
}
