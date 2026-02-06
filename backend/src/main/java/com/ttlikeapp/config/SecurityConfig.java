package com.ttlikeapp.config;

import com.ttlikeapp.security.JwtAuthenticationEntryPoint;
import com.ttlikeapp.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration - JWT-based authentication with Spring Security
 * 
 * Security Model:
 * - Stateless JWT authentication (no server-side sessions)
 * - Public endpoints for browsing, likes, registration
 * - Protected endpoints for content creation/modification
 * - Role-based authorization ready for future Admin features
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (JWT is stateless, no session-based CSRF risk)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure request authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers(HttpMethod.GET, "/videos/**", "/users/*/profile", "/feed/**").permitAll()
                .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/ws/**").permitAll()  // WebSocket handshake
                
                // Protected endpoints (authentication required)
                .requestMatchers(HttpMethod.POST, "/videos/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/videos/**", "/users/me").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/videos/**", "/users/me").authenticated()
                .requestMatchers("/likes/**", "/comments/**", "/follows/**").authenticated()
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            
            // Session management - stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Exception handling - custom entry point for JWT errors
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(entryPoint)
            )
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        // For H2 console (development only)
        http.headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    /**
     * Password encoder using BCrypt
     * 10 rounds default, can be adjusted for security/performance balance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider for DAO-based authentication
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
