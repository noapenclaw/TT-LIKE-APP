package com.ttlikeapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Feed Request DTO
 * Pagination and filtering parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedRequest {

    @Min(value = 0, message = "Page cannot be negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 50, message = "Size cannot exceed 50")
    @Builder.Default
    private Integer size = 10;

    // For "Following" feed - set to null for "For You" feed
    private Long userId;

    // Optional filters
    private String hashtag;
    private String sortBy;  // trending, recent, popular
}
