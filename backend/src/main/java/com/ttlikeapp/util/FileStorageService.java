package com.ttlikeapp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * File Storage Service - Local filesystem implementation
 * 
 * Used when S3 is disabled (development mode).
 * Stores files in a local directory with UUID-based naming.
 * 
 * Production: Swap for S3FileStorageService
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${aws.local-storage.path:./uploads}")
    private String storagePath;

    private final Path storageDirectory;

    public FileStorageService(@Value("${aws.local-storage.path:./uploads}") String storagePath) {
        this.storagePath = storagePath;
        this.storageDirectory = Paths.get(storagePath);
        init();
    }

    /**
     * Initialize storage directory
     */
    public void init() {
        try {
            Files.createDirectories(storageDirectory);
            Files.createDirectories(storageDirectory.resolve("videos"));
            Files.createDirectories(storageDirectory.resolve("thumbnails"));
            log.info("Local storage initialized at {}", storageDirectory.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    /**
     * Store video file and return URL
     */
    public String storeVideo(MultipartFile file, Long userId) {
        try {
            // Generate unique filename
            String extension = getExtension(file.getOriginalFilename());
            String filename = userId + "_" + UUID.randomUUID() + "." + extension;
            
            // Store in videos subdirectory
            Path targetLocation = storageDirectory.resolve("videos").resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Stored video: {}", filename);
            return "/uploads/videos/" + filename;
        } catch (IOException e) {
            log.error("Failed to store video", e);
            throw new RuntimeException("Failed to store video file", e);
        }
    }

    /**
     * Store thumbnail image
     */
    public String storeThumbnail(MultipartFile file, Long userId) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String filename = userId + "_" + UUID.randomUUID() + "." + extension;
            
            Path targetLocation = storageDirectory.resolve("thumbnails").resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Stored thumbnail: {}", filename);
            return "/uploads/thumbnails/" + filename;
        } catch (IOException e) {
            log.error("Failed to store thumbnail", e);
            throw new RuntimeException("Failed to store thumbnail", e);
        }
    }

    /**
     * Delete file by URL
     */
    public void delete(String fileUrl) {
        try {
            // Convert URL to path
            String relativePath = fileUrl.replace("/uploads/", "");
            Path filePath = storageDirectory.resolve(relativePath);
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    /**
     * Check if file exists
     */
    public boolean exists(String fileUrl) {
        String relativePath = fileUrl.replace("/uploads/", "");
        Path filePath = storageDirectory.resolve(relativePath);
        return Files.exists(filePath);
    }

    /**
     * Get file path from URL
     */
    public Path getFilePath(String fileUrl) {
        String relativePath = fileUrl.replace("/uploads/", "");
        return storageDirectory.resolve(relativePath);
    }

    /**
     * Get file extension
     */
    private String getExtension(String filename) {
        if (filename == null) return "mp4";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex < 0 ? "mp4" : filename.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * Clean up all storage (for testing)
     */
    public void deleteAll() {
        try {
            Files.walk(storageDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("Failed to delete file", e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to clean storage", e);
        }
    }
}
