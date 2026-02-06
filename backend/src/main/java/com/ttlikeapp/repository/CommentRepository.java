package com.ttlikeapp.repository;

import com.ttlikeapp.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment Repository - Data access for video comments
 * 
 * Query Patterns:
 * - Top-level comments for video
 * - Replies to specific comment
 * - User's comments
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Get top-level comments for a video
     */
    @Query("SELECT c FROM Comment c WHERE c.video.id = :videoId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelByVideoId(@Param("videoId") Long videoId, Pageable pageable);

    /**
     * Get all comments for a video (including replies, ordered by creation)
     */
    @Query("SELECT c FROM Comment c WHERE c.video.id = :videoId ORDER BY c.createdAt DESC")
    Page<Comment> findAllByVideoId(@Param("videoId") Long videoId, Pageable pageable);

    /**
     * Get threaded comments with replies
     */
    @Query("""
        SELECT c FROM Comment c 
        LEFT JOIN FETCH c.replies r
        WHERE c.video.id = :videoId 
        AND c.parent IS NULL 
        ORDER BY c.createdAt DESC
        """)
    List<Comment> findThreadedComments(@Param("videoId") Long videoId, Pageable pageable);

    /**
     * Get replies to a specific comment
     */
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * Count comments for video
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.video.id = :videoId AND c.isDeleted = false")
    Long countActiveByVideoId(@Param("videoId") Long videoId);

    /**
     * Count replies for a comment
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parent.id = :parentId AND c.isDeleted = false")
    Long countRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * Get user's comments
     */
    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    Page<Comment> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Delete all comments for a video (used when video is deleted)
     */
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.video.id = :videoId")
    void deleteAllByVideoId(@Param("videoId") Long videoId);
}
