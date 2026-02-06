package com.ttlikeapp.repository;

import com.ttlikeapp.entity.Like;
import com.ttlikeapp.entity.User;
import com.ttlikeapp.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Like Repository - Data access for video likes
 * 
 * Query Patterns:
 * - Check if user liked video
 * - Count likes per video
 * - Get liked videos for user
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * Check if user liked video
     */
    boolean existsByUserAndVideo(User user, Video video);

    /**
     * Check by IDs (for authenticated requests)
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Like l WHERE l.user.id = :userId AND l.video.id = :videoId")
    boolean existsByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") Long videoId);

    /**
     * Find specific like entry
     */
    Optional<Like> findByUserAndVideo(User user, Video video);

    /**
     * Find by IDs
     */
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.video.id = :videoId")
    Optional<Like> findByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") Long videoId);

    /**
     * Count likes for a video
     */
    @Query("SELECT COUNT(l) FROM Like l WHERE l.video.id = :videoId")
    Long countByVideoId(@Param("videoId") Long videoId);

    /**
     * Get user's liked videos
     */
    @Query("SELECT l.video FROM Like l WHERE l.user.id = :userId ORDER BY l.createdAt DESC")
    Page<com.ttlikeapp.entity.Video> findLikedVideosByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Delete like by user and video
     */
    @Modifying
    @Query("DELETE FROM Like l WHERE l.user.id = :userId AND l.video.id = :videoId")
    void deleteByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") Long videoId);

    /**
     * Get users who liked a video (for social features)
     */
    @Query("SELECT l.user FROM Like l WHERE l.video.id = :videoId ORDER BY l.createdAt DESC")
    Page<User> findUsersByVideoId(@Param("videoId") Long videoId, Pageable pageable);
}
