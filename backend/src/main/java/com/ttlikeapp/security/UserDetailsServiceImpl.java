package com.ttlikeapp.security;

import com.ttlikeapp.entity.User;
import com.ttlikeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserDetailsService Implementation
 * 
 * Loads user details by username or email for Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try username first, then email
        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> userRepository.findByEmailAndActiveTrue(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username/email: " + username
                        )));

        return UserPrincipal.create(user);
    }

    /**
     * Load by ID (for JWT filter)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }
}
