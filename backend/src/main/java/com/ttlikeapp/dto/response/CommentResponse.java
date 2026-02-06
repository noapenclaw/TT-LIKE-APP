package com.ttlikeapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comment Response DTO
 * Includes reply threading information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private UserSummaryResponse user;
    
    // Threading
    private Long parentId;
    private List<CommentResponse> replies;
    private Integer depth;
    
    // Engagement
    private Long likesCount;
    private Long repliesCount;
    private Boolean hasLiked;
    
    // Moderation
    private Boolean isDeleted;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
