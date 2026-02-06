package com.ttlikeapp.repository;

import com.ttlikeapp.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserAuth Repository - Data access for authentication information
 * 
 * Methods:
 * - Find by user ID
 * - Find by provider ID (for OAuth)
 */
@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    /**
     * Find authentication record by user ID
     */
    Optional<UserAuth> findByUserId(Long userId);

    /**
     * Find by provider ID for OAuth
     */
    Optional<UserAuth> findByProviderId(String providerId);

    /**
     * Update refresh token
     */
    @Modifying
    @Query("UPDATE UserAuth ua SET ua.refreshToken = :token, ua.refreshTokenExpiresAt = :expiresAt WHERE ua.user.id = :userId")
    void updateRefreshToken(@Param("userId") Long userId, @Param("token") String token, @Param("expiresAt") java.time.LocalDateTime expiresAt);

    /**
     * Clear refresh token (logout)
     */
    @Modifying
    @Query("UPDATE UserAuth ua SET ua.refreshToken = NULL, ua.refreshTokenExpiresAt = NULL WHERE ua.refreshToken = :token")
    void invalidateRefreshToken(@Param("token") String token);

    /**
     * Check if email verification token exists
     */
    boolean existsByEmailVerificationToken(String token);
}
