package com.ttlikeapp.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ttlikeapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * User Principal - Spring Security UserDetails implementation
 * 
 * Wraps our User entity to work with Spring Security infrastructure.
 * Supports future role-based authorization expansion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    @JsonIgnore
    private String email;
    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Create UserPrincipal from User entity
     */
    public static UserPrincipal create(User user) {
        // Future: Load roles from database
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(null)  // Don't expose password in token/JSON
                .authorities(authorities)
                .build();
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
