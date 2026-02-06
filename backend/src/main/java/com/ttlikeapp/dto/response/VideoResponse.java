package com.ttlikeapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Video Response DTO
 * Full video metadata with user info and engagement counts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {

    private Long id;
    private String videoUrl;
    private String thumbnailUrl;
    private String caption;
    private Integer duration;
    private Integer width;
    private Integer height;
    
    // Author info
    private UserSummaryResponse user;
    
    // Engagement
    private Long viewsCount;
    private Long likesCount;
    private Long commentsCount;
    private Long sharesCount;
    private Long savesCount;
    
    // User's interaction (if authenticated)
    private Boolean hasLiked;
    private Boolean hasSaved;
    
    // Metadata
    private Set<String> hashtags;
    private Boolean allowComments;
    private Boolean allowDuet;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
