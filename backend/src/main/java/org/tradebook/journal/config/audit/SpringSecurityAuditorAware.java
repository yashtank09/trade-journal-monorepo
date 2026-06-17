package org.tradebook.journal.config.audit;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.tradebook.journal.config.security.AppUserDetails;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<Long> {

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        // Use the custom UserDetails we created earlier to get the ID efficiently
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        return Optional.ofNullable(userDetails.getId());
    }
}
