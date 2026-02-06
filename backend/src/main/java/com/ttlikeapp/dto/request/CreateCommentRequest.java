package com.ttlikeapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Comment Request DTO
 * Supports both top-level comments and replies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String content;

    @NotNull(message = "Video ID is required")
    private Long videoId;

    // Optional - if provided, this is a reply to another comment
    private Long parentCommentId;
}
