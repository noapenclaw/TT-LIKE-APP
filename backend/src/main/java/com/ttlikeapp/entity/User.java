package com.ttlikeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User Entity - Core domain entity representing platform users.
 * 
 * Design Decisions:
 * - Uses JPA auditing for automatic timestamp management
 * - Soft delete pattern via 'active' flag (not physical deletion)
 * - Profile data denormalized for read performance
 * - Separate entity for sensitive auth data (UserAuth)
 * 
 * Relationships:
 * - One-to-Many: Videos (user's uploaded content)
 * - Many-to-Many: Follows (self-referential for following/followers)
 * 
 * @author TT-Like-App Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    @Column(name = "display_name", length = 100)
    private String displayName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    @Column(length = 500)
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "tiktok_id", length = 50)
    private String tiktokId;  // External platform integration

    @Column(name = "verified")
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "private_account")
    @Builder.Default
    private Boolean privateAccount = false;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    // Statistics (denormalized for performance)
    @Column(name = "followers_count")
    @Builder.Default
    private Long followersCount = 0L;

    @Column(name = "following_count")
    @Builder.Default
    private Long followingCount = 0L;

    @Column(name = "videos_count")
    @Builder.Default
    private Long videosCount = 0L;

    @Column(name = "total_likes_received")
    @Builder.Default
    private Long totalLikesReceived = 0L;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("createdAt DESC")
    private List<Video> videos = new ArrayList<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Follow> followers = new HashSet<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();

    /**
     * Convenience method to add a video
     */
    public void addVideo(Video video) {
        videos.add(video);
        video.setUser(this);
        this.videosCount++;
    }

    /**
     * Convenience method to remove a video
     */
    public void removeVideo(Video video) {
        videos.remove(video);
        video.setUser(null);
        this.videosCount--;
    }

    /**
     * Soft delete - marks user as inactive rather than physical deletion
     */
    public void softDelete() {
        this.active = false;
        this.username = "deleted_" + this.id + "_" + System.currentTimeMillis();
        this.email = "deleted_" + this.id + "@deleted.local";
    }

    /**
     * Update statistics after follow change
     */
    public void incrementFollowers() {
        this.followersCount++;
    }

    public void decrementFollowers() {
        if (this.followersCount > 0) {
            this.followersCount--;
        }
    }

    public void incrementFollowing() {
        this.followingCount++;
    }

    public void decrementFollowing() {
        if (this.followingCount > 0) {
            this.followingCount--;
        }
    }

    public void incrementTotalLikes() {
        this.totalLikesReceived++;
    }

    public void decrementTotalLikes() {
        if (this.totalLikesReceived > 0) {
            this.totalLikesReceived--;
        }
    }
}
