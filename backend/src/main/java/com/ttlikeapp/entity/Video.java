package com.ttlikeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Video Entity - Core content entity representing short-form videos.
 * 
 * Design Decisions:
 * - Separates video metadata from storage paths (allows multiple storage backends)
 * - Denormalized engagement metrics for fast read operations
 * - Soft delete pattern for content moderation
 * - Indexed for feed queries (created_at, user_id, hashtags)
 * 
 * Storage Abstraction:
 * - videoUrl: Primary CDN/streaming URL
 * - thumbnailUrl: Preview image
 * - originalUrl: Raw uploaded file (for reprocessing)
 * 
 * Performance Optimizations:
 * - Engagement counters updated asynchronously
 * - Hashtags extracted and indexed for discovery
 * 
 * @author TT-Like-App Team
 * @since 1.0.0
 */
@Entity
@Table(name = "videos", indexes = {
    @Index(name = "idx_video_user", columnList = "user_id"),
    @Index(name = "idx_video_created", columnList = "created_at"),
    @Index(name = "idx_video_active", columnList = "active"),
    @Index(name = "idx_video_private", columnList = "is_private"),
    @Index(name = "idx_video_tiktok", columnList = "tiktok_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Video URL is required")
    @Column(name = "video_url", nullable = false, length = 1000)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "original_url", length = 1000)
    private String originalUrl;  // Raw upload before processing

    @Size(max = 200, message = "Caption cannot exceed 200 characters")
    @Column(length = 500)
    private String caption;

    @Column(name = "tiktok_id", length = 50)
    private String tiktokId;  // Import from external platform

    @NotNull(message = "Duration is required")
    @Column(nullable = false)
    private Integer duration;  // in seconds

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "file_size")
    private Long fileSize;  // in bytes

    @Size(max = 100, message = "Format must be 100 chars or less")
    @Column(length = 100)
    private String format;  // e.g., "mp4", "mov"

    @Column(name = "is_private")
    @Builder.Default
    private Boolean isPrivate = false;

    @Column(name = "allow_comments")
    @Builder.Default
    private Boolean allowComments = true;

    @Column(name = "allow_duet")
    @Builder.Default
    private Boolean allowDuet = true;

    @Column(name = "allow_stitch")
    @Builder.Default
    private Boolean allowStitch = true;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    @Column(name = "review_status", length = 20)
    @Builder.Default
    private String reviewStatus = "APPROVED";  // APPROVED, PENDING, REJECTED

    // Engagement Metrics (denormalized for performance)
    @Column(name = "views_count")
    @Builder.Default
    private Long viewsCount = 0L;

    @Column(name = "likes_count")
    @Builder.Default
    private Long likesCount = 0L;

    @Column(name = "comments_count")
    @Builder.Default
    private Long commentsCount = 0L;

    @Column(name = "shares_count")
    @Builder.Default
    private Long sharesCount = 0L;

    @Column(name = "saves_count")
    @Builder.Default
    private Long savesCount = 0L;

    // For You Page Algorithm Metrics
    @Column(name = "engagement_score")
    @Builder.Default
    private Double engagementScore = 0.0;

    @Column(name = "viral_score")
    @Builder.Default
    private Double viralScore = 0.0;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;  // Video processing completion

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("createdAt DESC")
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("createdAt DESC")
    private Set<Comment> comments = new HashSet<>();

    // Hashtags extracted from caption
    @ElementCollection
    @CollectionTable(name = "video_hashtags", joinColumns = @JoinColumn(name = "video_id"))
    @Column(name = "hashtag")
    @Builder.Default
    private Set<String> hashtags = new HashSet<>();

    /**
     * Extract hashtags from caption before saving
     */
    @PrePersist
    @PreUpdate
    public void preProcess() {
        if (caption != null) {
            hashtags.clear();
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#(\\w+)");
            java.util.regex.Matcher matcher = pattern.matcher(caption);
            while (matcher.find()) {
                hashtags.add(matcher.group(1).toLowerCase());
            }
        }
    }

    /**
     * Soft delete video
     */
    public void softDelete() {
        this.active = false;
        this.videoUrl = null;  // Remove access to content
    }

    /**
     * Recalculate engagement score for FYP algorithm
     */
    public void calculateEngagementScore() {
        // Weighted engagement formula
        double likesWeight = 1.0;
        double commentsWeight = 2.0;
        double sharesWeight = 3.0;
        double savesWeight = 2.5;
        
        // Calculate score based on views
        if (viewsCount > 0) {
            this.engagementScore = (
                (likesCount * likesWeight) +
                (commentsCount * commentsWeight) +
                (sharesCount * sharesWeight) +
                (savesCount * savesWeight)
            ) / viewsCount;
        }
        
        // Viral score includes time decay (simplified)
        this.viralScore = engagementScore * Math.log(viewsCount + 1);
    }

    /**
     * Increment view counter
     */
    public void incrementViews() {
        this.viewsCount++;
    }

    public void incrementLikes() {
        this.likesCount++;
        user.incrementTotalLikes();
    }

    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
            user.decrementTotalLikes();
        }
    }

    public void incrementComments() {
        this.commentsCount++;
    }

    public void decrementComments() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }

    public void incrementShares() {
        this.sharesCount++;
    }

    public void incrementSaves() {
        this.savesCount++;
    }
}
