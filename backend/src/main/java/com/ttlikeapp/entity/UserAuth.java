package com.ttlikeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * UserAuth Entity - Separates authentication data from user profile.
 * 
 * Security Design:
 * - Passwords stored as BCrypt hashes only
 * - Separate entity allows different caching/security rules
 * - Failed login tracking for rate limiting
 * - Refresh tokens for session management
 * 
 * Separation of Concerns:
 * - User: Public profile data, can be cached aggressively
 * - UserAuth: Sensitive credentials, minimal caching
 * 
 * @author TT-Like-App Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_auth", indexes = {
    @Index(name = "idx_auth_user", columnList = "user_id", unique = true),
    @Index(name = "idx_auth_provider", columnList = "auth_provider")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Password hash is required")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotNull(message = "Auth provider is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 100)
    private String providerId;  // External ID from OAuth provider

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    // Security tracking
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // Email verification
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;

    // 2FA (future)
    @Column(name = "two_factor_enabled")
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", length = 255)
    private String twoFactorSecret;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Authentication provider types
     */
    public enum AuthProvider {
        LOCAL,      // Email/password
        GOOGLE,     // Google OAuth
        FACEBOOK,   // Facebook OAuth
        APPLE,      // Sign in with Apple
        TIKTOK      // TikTok OAuth
    }

    /**
     * Record failed login attempt
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(15);
        }
    }

    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(String ipAddress) {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginIp = ipAddress;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Check if account is currently locked
     */
    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lockedUntil);
    }

    /**
     * Update password
     */
    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = LocalDateTime.now();
        this.refreshToken = null;  // Invalidate existing sessions
    }

    /**
     * Generate and set refresh token
     */
    public String generateRefreshToken() {
        this.refreshToken = java.util.UUID.randomUUID().toString();
        this.refreshTokenExpiresAt = LocalDateTime.now().plusDays(7);
        return this.refreshToken;
    }

    /**
     * Check if refresh token is valid
     */
    public boolean isRefreshTokenValid(String token) {
        if (this.refreshToken == null || !this.refreshToken.equals(token)) {
            return false;
        }
        return refreshTokenExpiresAt == null || 
               LocalDateTime.now().isBefore(refreshTokenExpiresAt);
    }

    /**
     * Invalidate refresh token (logout)
     */
    public void invalidateRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }
}
