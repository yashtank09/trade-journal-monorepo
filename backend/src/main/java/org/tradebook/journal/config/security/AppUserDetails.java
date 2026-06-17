package org.tradebook.journal.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tradebook.journal.features.auth.entity.User;

import java.util.Collection;
import java.util.List;

@Getter
public class AppUserDetails implements UserDetails {

    private final User user;

    public AppUserDetails(User user) {
        this.user = user;
    }

    /**
     * Expose the ID directly so controllers can access it
     * efficiently without hitting the database.
     */
    public Long getId() {
        return user.getId();
    }

    public String getCurrency() {
        return user.getCurrency();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
