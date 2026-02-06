package com.ttlikeapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Video Feed Response DTO
 * Paginated list of videos with metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFeedResponse {

    private List<VideoResponse> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean last;
    private Boolean first;
    
    // Feed type info
    private String feedType;  // "FOR_YOU" or "FOLLOWING"
    private String algorithmVersion;  // For debugging/recommendation tracking
}
