package org.tradebook.journal.features.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.features.auth.entity.PasswordResetToken;
import org.tradebook.journal.features.auth.entity.User;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
