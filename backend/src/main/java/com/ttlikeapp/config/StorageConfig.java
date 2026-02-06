package com.ttlikeapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Storage Configuration - AWS S3 or compatible (MinIO, LocalStack)
 * 
 * Design allows local development without AWS:
 * - Disable S3: uses local filesystem storage
 * - Enable S3: uses real AWS or S3-compatible service
 */
@Configuration
public class StorageConfig {

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${aws.s3.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;  // For MinIO/local stack

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    /**
     * S3 Client configuration
     * Supports both AWS and S3-compatible services
     */
    @Bean
    public S3Client s3Client() {
        if (!s3Enabled) {
            return null;  // Local storage mode
        }

        Region region = Region.of(awsRegion);
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        S3Client.Builder clientBuilder = S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials));
        
        // If endpoint provided (MinIO/LocalStack), configure path-style access
        if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
            S3Configuration serviceConfiguration = S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build();
            
            clientBuilder.endpointOverride(URI.create(s3Endpoint))
                    .serviceConfiguration(serviceConfiguration);
        }
        
        return clientBuilder.build();
    }

    @Bean
    public boolean isS3Enabled() {
        return s3Enabled;
    }

    @Bean
    public String s3BucketName(@Value("${aws.s3.bucket:tt-like-app-videos}") String bucketName) {
        return bucketName;
    }
}
