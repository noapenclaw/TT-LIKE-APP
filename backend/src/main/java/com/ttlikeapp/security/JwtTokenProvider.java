package com.ttlikeapp.security;

import com.ttlikeapp.repository.UserAuthRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider - Handles token generation and validation
 * 
 * Token Structure:
 * - sub: user ID
 * - username: username
 * - jti: unique token ID for revocation
 * - iat: issued at
 * - exp: expiration
 * 
 * @author TT-Like-App Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final SecretKey jwtSecretKey;
    private final UserAuthRepository userAuthRepository;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * Generate access token from Authentication
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromUserId(userPrincipal.getId(), userPrincipal.getUsername());
    }

    /**
     * Generate token from user details
     */
    public String generateTokenFromUserId(Long userId, String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusSeconds(jwtExpiration / 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .expiration(Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(jwtSecretKey)
                .compact();
    }

    /**
     * Generate refresh token (stored in database)
     */
    public String generateRefreshToken(Long userId) {
        String refreshToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusDays(7);
        
        userAuthRepository.updateRefreshToken(userId, refreshToken, expiry);
        
        log.info("Generated refresh token for user {}", userId);
        return refreshToken;
    }

    /**
     * Validate token and check expiration
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract user ID from token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("username", String.class);
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    /**
     * Get time until expiration in milliseconds
     */
    public long getExpirationTime(String token) {
        Date expiration = getExpirationDate(token);
        return expiration.getTime() - System.currentTimeMillis();
    }
}
