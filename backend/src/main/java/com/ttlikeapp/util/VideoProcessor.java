package com.ttlikeapp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Video Processing Utility
 * 
 * Extracts metadata from video files using ffprobe (FFmpeg).
 * In production, consider using cloud-based transcoding services
 * like AWS Elemental MediaConvert or similar.
 * 
 * Dependencies: ffmpeg installed on system
 */
@Component
@Slf4j
public class VideoProcessor {

    /**
     * Video metadata extracted from file
     */
    public record VideoMetadata(
            int duration,      // seconds
            int width,         // pixels
            int height,        // pixels
            long fileSize,     // bytes
            String format      // mp4, mov, etc.
    ) {}

    /**
     * Extract metadata from uploaded video
     */
    public VideoMetadata extractMetadata(MultipartFile file) {
        try {
            // Save temporarily for processing
            Path tempFile = Files.createTempFile("video_", "." + getExtension(file.getOriginalFilename()));
            file.transferTo(tempFile.toFile());

            VideoMetadata metadata = extractWithFfprobe(tempFile, file.getSize());
            
            // Cleanup
            Files.deleteIfExists(tempFile);
            
            return metadata;

        } catch (Exception e) {
            log.error("Failed to extract video metadata", e);
            // Return basic metadata as fallback
            return new VideoMetadata(
                    0,
                    1080,  // default
                    1920,  // default
                    file.getSize(),
                    getExtension(file.getOriginalFilename())
            );
        }
    }

    /**
     * Use ffprobe to get video metadata
     */
    private VideoMetadata extractWithFfprobe(Path videoPath, long fileSize) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height,duration",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1",
                    videoPath.toString()
            );

            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                int width = 0, height = 0;
                double duration = 0;

                while ((line = reader.readLine()) != null) {
                    log.debug("ffprobe output: {}", line);
                    
                    if (line.startsWith("width=")) {
                        width = Integer.parseInt(line.substring(6).trim());
                    } else if (line.startsWith("height=")) {
                        height = Integer.parseInt(line.substring(7).trim());
                    } else if (line.startsWith("duration=")) {
                        String durationStr = line.substring(9).trim();
                        try {
                            duration = Double.parseDouble(durationStr);
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse duration: {}", durationStr);
                        }
                    }
                }

                process.waitFor();

                return new VideoMetadata(
                        (int) Math.round(duration),
                        width,
                        height,
                        fileSize,
                        getExtension(videoPath.getFileName().toString())
                );
            }

        } catch (Exception e) {
            log.error("ffprobe failed, using fallback metadata", e);
            return new VideoMetadata(0, 1080, 1920, fileSize, "mp4");
        }
    }

    /**
     * Generate thumbnail from video (requires ffmpeg)
     */
    public byte[] generateThumbnail(Path videoPath, String filename) {
        try {
            Path thumbnailPath = Files.createTempFile("thumb_", ".jpg");
            
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath.toString(),
                    "-ss", "00:00:01",  // 1 second in
                    "-vframes", "1",
                    "-q:v", "2",
                    thumbnailPath.toString()
            );

            Process process = pb.start();
            process.waitFor();

            byte[] thumbnailBytes = Files.readAllBytes(thumbnailPath);
            Files.deleteIfExists(thumbnailPath);
            
            return thumbnailBytes;

        } catch (Exception e) {
            log.error("Failed to generate thumbnail", e);
            return null;
        }
    }

    /**
     * Validate video file
     */
    public boolean isValidVideo(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("video/");
    }

    /**
     * Get max allowed video duration
     */
    public int getMaxDuration() {
        return 180; // 3 minutes (TikTok-style)
    }

    /**
     * Get max file size (in bytes)
     */
    public long getMaxFileSize() {
        return 100 * 1024 * 1024; // 100MB
    }

    private String getExtension(String filename) {
        if (filename == null) return "mp4";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex < 0 ? "mp4" : filename.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * Check if FFmpeg is available
     */
    public boolean isFfprobeAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffprobe", "-version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
