package com.ttlikeapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Full User Profile Response DTO
 * Used for profile pages and detailed user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private Boolean verified;
    private Boolean privateAccount;
    
    // Statistics
    private Long followersCount;
    private Long followingCount;
    private Long videosCount;
    private Long totalLikesReceived;
    
    // Social context (if requesting user is authenticated)
    private Boolean isFollowing;  // Is current user following this user?
    private Boolean isFollowedBy; // Is this user following current user?
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
