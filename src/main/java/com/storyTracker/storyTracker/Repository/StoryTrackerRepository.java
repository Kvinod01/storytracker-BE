package com.storyTracker.storyTracker.Repository;

import com.storyTracker.storyTracker.Entity.StoryTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoryTrackerRepository extends JpaRepository<StoryTracker, UUID> {
}
