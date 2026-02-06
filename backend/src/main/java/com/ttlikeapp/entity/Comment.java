package com.ttlikeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment Entity - Supports nested replies for threaded discussions.
 * 
 * Design Decisions:
 * - Self-referential relationship for nested replies
 * - Path enumeration for efficient tree traversal
 * - Soft delete for moderation (content nulled but structure preserved)
 * - Like count denormalized for performance
 * 
 * Tree Structure:
 * - parent = null: Top-level comment
 * - parent != null: Reply to another comment
 * - replies: List of direct children
 * 
 * @author TT-Like-App Team
 * @since 1.0.0
 */
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_video", columnList = "video_id"),
    @Index(name = "idx_comment_user", columnList = "user_id"),
    @Index(name = "idx_comment_parent", columnList = "parent_id"),
    @Index(name = "idx_comment_created", columnList = "created_at"),
    @Index(name = "idx_comment_path", columnList = "path")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Comment content is required")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    // Self-referential relationship for nested replies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("createdAt ASC")
    private List<Comment> replies = new ArrayList<>();

    // Path enumeration for efficient tree queries (e.g., "1.2.5.")
    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "depth")
    @Builder.Default
    private Integer depth = 0;

    // Soft delete flag
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    // Engagement
    @Column(name = "likes_count")
    @Builder.Default
    private Long likesCount = 0L;

    @Column(name = "replies_count")
    @Builder.Default
    private Long repliesCount = 0L;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Add a reply to this comment
     */
    public void addReply(Comment reply) {
        replies.add(reply);
        reply.setParent(this);
        reply.setDepth(this.depth + 1);
        reply.setPath(this.path + this.id + ".");
        reply.setVideo(this.video);
        this.repliesCount++;
    }

    /**
     * Remove a reply
     */
    public void removeReply(Comment reply) {
        replies.remove(reply);
        reply.setParent(null);
        this.repliesCount--;
    }

    /**
     * Soft delete - preserves thread structure but removes content
     */
    public void softDelete() {
        this.isDeleted = true;
        this.content = "[deleted]";
    }

    /**
     * Check if this is a top-level comment
     */
    public boolean isTopLevel() {
        return parent == null;
    }

    /**
     * Increment likes counter
     */
    public void incrementLikes() {
        this.likesCount++;
    }

    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.path == null) {
            this.path = "";
        }
        if (this.depth == null) {
            this.depth = 0;
        }
    }
}
