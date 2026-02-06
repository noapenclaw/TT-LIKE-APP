package com.ttlikeapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Like Entity - Represents a user's like on a video.
 * 
 * Design Decisions:
 * - Composite unique constraint prevents duplicate likes
 * - No update timestamp (immutable once created)
 * - Cascade operations managed at service layer for counter consistency
 * 
 * Performance:
 * - Indexed on user_id for "liked videos" queries
 * - Indexed on video_id for "who liked this" queries
 * 
 * @author TT-Like-App Team
 * @since 1.0.0
 */
@Entity
@Table(name = "likes", 
    indexes = {
        @Index(name = "idx_like_user", columnList = "user_id"),
        @Index(name = "idx_like_video", columnList = "video_id"),
        @Index(name = "idx_like_created", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "video_id"}, name = "unique_user_video_like")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Convenience constructor
     */
    public Like(User user, Video video) {
        this.user = user;
        this.video = video;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Like like = (Like) o;
        return user != null && video != null &&
               user.getId().equals(like.user.getId()) &&
               video.getId().equals(like.video.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(user != null ? user.getId() : null, 
                                      video != null ? video.getId() : null);
    }
}
