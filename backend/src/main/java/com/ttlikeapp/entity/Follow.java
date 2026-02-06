package com.ttlikeapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Follow Entity - Represents a following relationship between users.
 * 
 * Design Decisions:
 * - Composite unique constraint prevents duplicate follows
 * - Follower follows Following (naming might seem reversed but matches Instagram/Twitter convention)
 * - No status enum (always immediate, no follow requests for this MVP)
 * - Indexed for "following" and "followers" queries
 * 
 * Relationship:
 * - follower_id: The user who is doing the following
 * - following_id: The user being followed
 * 
 * @author TT-Like-App Team
 * @since 1.0.0
 */
@Entity
@Table(name = "follows",
    indexes = {
        @Index(name = "idx_follow_follower", columnList = "follower_id"),
        @Index(name = "idx_follow_following", columnList = "following_id"),
        @Index(name = "idx_follow_created", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "following_id"}, 
                         name = "unique_follow_relationship")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who is following (follower)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    // The user being followed (followee)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Convenience constructor
     */
    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Follow follow = (Follow) o;
        return follower != null && following != null &&
               follower.getId().equals(follow.follower.getId()) &&
               following.getId().equals(follow.following.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(follower != null ? follower.getId() : null,
                                      following != null ? following.getId() : null);
    }
}
