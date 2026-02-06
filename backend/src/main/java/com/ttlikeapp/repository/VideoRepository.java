package com.ttlikeapp.repository;

import com.ttlikeapp.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Video Repository - Data access for video content
 * 
 * Query Patterns:
 * - Feed queries with pagination
 * - User video listings
 * - Trending/Popular algorithms
 * - Hashtag discovery
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    /**
     * Find active video by ID
     */
    @Query("SELECT v FROM Video v WHERE v.id = :id AND v.active = true AND v.reviewStatus = 'APPROVED'")
    Optional<Video> findActiveById(@Param("id") Long id);

    /**
     * Get user's videos paginated
     */
    @Query("SELECT v FROM Video v WHERE v.user.id = :userId AND v.active = true AND v.isPrivate = false ORDER BY v.createdAt DESC")
    Page<Video> findPublicByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Get user's videos including private (for owner)
     */
    @Query("SELECT v FROM Video v WHERE v.user.id = :userId AND v.active = true ORDER BY v.createdAt DESC")
    Page<Video> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Following feed - videos from followed users
     */
    @Query("""
        SELECT v FROM Video v 
        WHERE v.user.id IN 
            (SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId)
        AND v.active = true 
        AND v.isPrivate = false
        AND v.reviewStatus = 'APPROVED'
        ORDER BY v.createdAt DESC
        """)
    Page<Video> findFollowingFeed(@Param("userId") Long userId, Pageable pageable);

    /**
     * Trending videos algorithm
     */
    @Query("""
        SELECT v FROM Video v 
        WHERE v.active = true 
        AND v.isPrivate = false
        AND v.reviewStatus = 'APPROVED'
        AND v.createdAt > :since
        ORDER BY (v.likesCount * 1.0 + v.commentsCount * 2.0 + v.sharesCount * 3.0) / 
                 (EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - v.createdAt)) / 3600 + 1) DESC
        """)
    Page<Video> findTrending(@Param("since") java.time.LocalDateTime since, Pageable pageable);

    /**
     * Popular videos - all time
     */
    @Query("""
        SELECT v FROM Video v 
        WHERE v.active = true 
        AND v.isPrivate = false
        AND v.reviewStatus = 'APPROVED'
        ORDER BY (v.engagementScore * v.viewsCount) DESC
        """)
    Page<Video> findPopular(Pageable pageable);

    /**
     * Most recent videos
     */
    @Query("""
        SELECT v FROM Video v 
        WHERE v.active = true 
        AND v.isPrivate = false
        AND v.reviewStatus = 'APPROVED'
        ORDER BY v.createdAt DESC
        """)
    Page<Video> findRecent(Pageable pageable);

    /**
     * Search by hashtag
     */
    @Query("""
        SELECT DISTINCT v FROM Video v 
        JOIN v.hashtags h
        WHERE h = :hashtag
        AND v.active = true 
        AND v.isPrivate = false
        AND v.reviewStatus = 'APPROVED'
        ORDER BY v.createdAt DESC
        """)
    Page<Video> searchByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    /**
     * Discover feed - videos NOT from followed users (to find new content)
     */
    @Query("""
        SELECT v FROM Video v 
        WHERE v.user.id NOT IN 
            (SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId)
        AND v.user.id != :userId
        AND v.active = true 
        AND v.isPrivate = false
        AND v.reviewStatus = 'APPROVED'
        ORDER BY v.engagementScore DESC
        """)
    Page<Video> findDiscoverFeed(@Param("userId") Long userId, Pageable pageable);

    /**
     * Get videos by user IDs (for feed building)
     */
    @Query("""
        SELECT v FROM Video v 
        WHERE v.user.id IN :userIds
        AND v.active = true 
        AND v.isPrivate = false
        AND v.reviewStatus = 'APPROVED'
        ORDER BY v.createdAt DESC
        """)
    List<Video> findByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Count videos by user
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.user.id = :userId AND v.active = true")
    Long countActiveByUserId(@Param("userId") Long userId);
}
