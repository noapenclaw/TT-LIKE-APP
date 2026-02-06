package com.ttlikeapp.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

/**
 * AWS S3 File Storage Implementation
 * 
 * Production-ready storage with cloud CDN capabilities.
 * Supports AWS S3, MinIO, and other S3-compatible services.
 * 
 * Fall back to FileStorageService if S3 is not enabled.
 * 
 * @author TT-Like-App Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket:tt-like-app-videos}")
    private String bucketName;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    /**
     * Store video in S3 bucket
     */
    public String storeVideo(MultipartFile file, Long userId) {
        if (!s3Enabled || s3Client == null) {
            throw new IllegalStateException("S3 not enabled");
        }

        String key = generateKey("videos", userId, file.getOriginalFilename());
        return uploadFile(file, key);
    }

    /**
     * Store thumbnail in S3 bucket
     */
    public String storeThumbnail(MultipartFile file, Long userId) {
        if (!s3Enabled || s3Client == null) {
            throw new IllegalStateException("S3 not enabled");
        }

        String key = generateKey("thumbnails", userId, file.getOriginalFilename());
        return uploadFile(file, key);
    }

    /**
     * Generate S3 key with user prefix for organization
     */
    private String generateKey(String type, Long userId, String filename) {
        String extension = getExtension(filename);
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%d/%s.%s", type, userId, uuid, extension);
    }

    /**
     * Upload file to S3
     */
    private String uploadFile(MultipartFile file, String key) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Get URL (virtual-hosted style)
            String url = String.format("https://s3.amazonaws.com/%s/%s", bucketName, key);
            
            log.info("Uploaded {} to S3: {}", key, url);
            return url;

        } catch (IOException e) {
            log.error("Failed to upload to S3: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Delete file from S3
     */
    public void delete(String fileUrl) {
        if (!s3Enabled || s3Client == null) {
            log.warn("S3 not enabled, skipping delete");
            return;
        }

        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted from S3: {}", key);

        } catch (Exception e) {
            log.error("Failed to delete from S3: {}", fileUrl, e);
        }
    }

    /**
     * Check if file exists in bucket
     */
    public boolean exists(String fileUrl) {
        if (!s3Enabled || s3Client == null) {
            return false;
        }

        try {
            String key = extractKeyFromUrl(fileUrl);
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Generate pre-signed URL for direct upload (mobile apps)
     */
    public String generatePresignedUrl(String key) {
        // Implementation depends on SDK version - placeholder for future
        throw new UnsupportedOperationException("Presigned URLs not yet implemented");
    }

    /**
     * Create bucket if it doesn't exist
     */
    public void ensureBucketExists() {
        if (!s3Enabled || s3Client == null) {
            return;
        }

        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
            log.info("S3 bucket {} exists", bucketName);
        } catch (NoSuchBucketException e) {
            CreateBucketRequest createRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(createRequest);
            log.info("Created S3 bucket: {}", bucketName);
        }
    }

    /**
     * Extract S3 key from URL
     * Supports both path and virtual-hosted style URLs
     */
    private String extractKeyFromUrl(String url) {
        // Remove protocol and domain
        if (url.contains("amazonaws.com")) {
            int keyStart = url.indexOf(".com/");
            if (keyStart > 0) {
                return url.substring(keyStart + 5);
            }
        }
        // Fallback: extract everything after bucket name
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private String getExtension(String filename) {
        if (filename == null) return "mp4";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex < 0 ? "mp4" : filename.substring(dotIndex + 1).toLowerCase();
    }
}
