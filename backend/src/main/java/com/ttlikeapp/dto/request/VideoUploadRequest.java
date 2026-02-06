package com.ttlikeapp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Video Upload Request DTO
 * Contains video file and optional metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadRequest {

    @NotNull(message = "Video file is required")
    private MultipartFile file;

    @Size(max = 200, message = "Caption cannot exceed 200 characters")
    private String caption;

    private Boolean isPrivate = false;
    private Boolean allowComments = true;
    private Boolean allowDuet = true;
    private Boolean allowStitch = true;

    // Optional thumbnail (auto-generated if not provided)
    private MultipartFile thumbnail;
}
