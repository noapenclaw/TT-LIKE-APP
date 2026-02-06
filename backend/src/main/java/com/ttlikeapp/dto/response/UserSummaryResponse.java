package com.ttlikeapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Summary Response DTO
 * Lightweight user info for embedding in other responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private Boolean verified;
}
