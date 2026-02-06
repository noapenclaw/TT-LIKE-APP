package com.ttlikeapp.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT Configuration
 * Centralizes JWT key and algorithm configuration
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:tt-like-app-super-secret-key-change-in-production}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")  // 24 hours
    private long jwtExpiration;

    /**
     * JWT signing key
     * Creates a secure HMAC key from the configured secret
     */
    @Bean
    public SecretKey jwtSecretKey() {
        // For JJWT 0.12.x, we need to ensure key is at least 256 bits for HS256
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public long jwtExpiration() {
        return jwtExpiration;
    }
}
