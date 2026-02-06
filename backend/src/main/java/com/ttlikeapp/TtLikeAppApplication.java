package com.ttlikeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * TT-LIKE-APP Main Application Entry Point
 * 
 * A TikTok-style short video platform demonstrating:
 * - Spring Boot 3.2 with Java 21
 * - JWT-based authentication
 * - AWS S3-compatible video storage
 * - Redis caching for feed performance
 * - WebSocket for real-time interactions
 * - Modular architecture for extensibility
 * 
 * @author TT-Like-App Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing  // Enables automatic @CreatedDate and @LastModifiedDate
@EnableCaching      // Enables Spring's annotation-driven cache management
public class TtLikeAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TtLikeAppApplication.class, args);
    }
}
