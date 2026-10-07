package com.storyTracker.storyTracker.Repository;

import com.storyTracker.storyTracker.Entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StoryRepository extends JpaRepository<Story, UUID> {
    /**
     * JPQL Query to count stories grouped by Priority for a given StoryTracker.
     * The result is a list of Object arrays: [Priority enum, Count]
     */
    @Query("SELECT s.priority, COUNT(s) FROM Story s " +
            "WHERE s.storyTracker.storyTrackerId = :trackerId " +
            "GROUP BY s.priority")
    List<Object[]> countStoriesByPriorityForTracker(@Param("trackerId") UUID trackerId);

    /**
     * JPQL to delete a specific Story by its ID and ensure it belongs to the correct Tracker.
     * The @Modifying annotation is required for all DML statements (UPDATE, DELETE, INSERT).
     */
    @Modifying
    @Query("DELETE FROM Story s WHERE s.id = :storyId AND s.storyTracker.storyTrackerId = :trackerId")
    int deleteStoryByIdAndTrackerId(@Param("storyId") UUID storyId, @Param("trackerId") UUID trackerId);

    // Method 1: Get Total Possible Weighted Score (for all stories)
    @Query(value = """
    SELECT SUM(
        CASE s.priority
            WHEN 'LOW' THEN 1
            WHEN 'MEDIUM' THEN 3
            WHEN 'HIGH' THEN 5
            ELSE 0
        END
    ) 
    FROM Story s 
    WHERE s.storyTracker.storyTrackerId = :trackerId
    """, nativeQuery = false) // Use nativeQuery=false for JPQL
    Long calculateTotalWeight(@Param("trackerId") UUID trackerId);

    // Method 2: Get Completed Weighted Score (for checked stories only)
    @Query(value = """
    SELECT SUM(
        CASE s.priority
            WHEN 'LOW' THEN 1
            WHEN 'MEDIUM' THEN 3
            WHEN 'HIGH' THEN 5
            ELSE 0
        END
    )
    FROM Story s
    WHERE s.storyTracker.storyTrackerId = :trackerId
    AND s.status = 'DONE'
    """, nativeQuery = false)
    Long calculateCompletedWeight(@Param("trackerId") UUID trackerId);
}
