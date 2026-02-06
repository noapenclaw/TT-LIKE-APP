package com.ttlikeapp.repository;

import com.ttlikeapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository - Data access layer for User entity
 * 
 * Extends JpaSpecificationExecutor for dynamic query building
 * Used by feed algorithms and user management features
 * 
 * @author TT-Like-App Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Find active user by username
     */
    Optional<User> findByUsernameAndActiveTrue(String username);

    /**
     * Find active user by email
     */
    Optional<User> findByEmailAndActiveTrue(String email);

    /**
     * Find user by username or email (for login)
     */
    @Query("SELECT u FROM User u WHERE (u.username = :credential OR u.email = :credential) AND u.active = true")
    Optional<User> findByUsernameOrEmail(@Param("credential") String credential);

    /**
     * Check if username exists (for registration validation)
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Search users by username containing substring
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:query% AND u.active = true ORDER BY u.followersCount DESC")
    Page<User> searchByUsername(@Param("query") String query, Pageable pageable);

    /**
     * Get suggested users to follow (popular, not following)
     */
    @Query("""
        SELECT u FROM User u 
        WHERE u.id NOT IN :excludedIds 
        AND u.active = true
        AND u.isPrivate = false
        ORDER BY u.followersCount DESC
        """)
    Page<User> findSuggestedUsers(@Param("excludedIds") List<Long> excludedIds, Pageable pageable);

    /**
     * Update follower count (optimized counter)
     */
    @Modifying
    @Query("UPDATE User u SET u.followersCount = u.followersCount + 1 WHERE u.id = :userId")
    void incrementFollowersCount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.followersCount = u.followersCount - 1 WHERE u.id = :userId AND u.followersCount > 0")
    void decrementFollowersCount(@Param("userId") Long userId);

    /**
     * Update following count
     */
    @Modifying
    @Query("UPDATE User u SET u.followingCount = u.followingCount + 1 WHERE u.id = :userId")
    void incrementFollowingCount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.followingCount = u.followingCount - 1 WHERE u.id = :userId AND u.followingCount > 0")
    void decrementFollowingCount(@Param("userId") Long userId);

    /**
     * Update video count
     */
    @Modifying
    @Query("UPDATE User u SET u.videosCount = u.videosCount + 1 WHERE u.id = :userId")
    void incrementVideosCount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.videosCount = u.videosCount - 1 WHERE u.id = :userId AND u.videosCount > 0")
    void decrementVideosCount(@Param("userId") Long userId);

    /**
     * Update total likes received
     */
    @Modifying
    @Query("UPDATE User u SET u.totalLikesReceived = u.totalLikesReceived + 1 WHERE u.id = :userId")
    void incrementTotalLikes(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.totalLikesReceived = u.totalLikesReceived - 1 WHERE u.id = :userId AND u.totalLikesReceived > 0")
    void decrementTotalLikes(@Param("userId") Long userId);

    /**
     * Find verified users
     */
    Page<User> findByVerifiedTrueAndActiveTrue(Pageable pageable);

    /**
     * Get total active user count
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    Long countActiveUsers();
}
