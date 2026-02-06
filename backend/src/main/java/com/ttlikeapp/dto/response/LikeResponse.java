package com.ttlikeapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Like Action Response DTO
 * Returns updated like state and count
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponse {

    private Long videoId;
    private Boolean liked;
    private Long likesCount;
}
