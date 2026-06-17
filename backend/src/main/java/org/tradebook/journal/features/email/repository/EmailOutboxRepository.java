package org.tradebook.journal.features.email.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.features.email.entity.EmailOutbox;

import java.util.List;

@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {
    
    /**
     * Finds emails by status, ordered by creation date ascending (oldest first).
     */
    List<EmailOutbox> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
