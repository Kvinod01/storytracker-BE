package com.storyTracker.storyTracker.Service;

import com.storyTracker.storyTracker.Dto.CreateStoryTrackerRequestDTO;
import com.storyTracker.storyTracker.Dto.StoryDeleteRequestDTO;
import com.storyTracker.storyTracker.Dto.StoryTrackerResponseDTO;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface StoryTrackerService {
    StoryTrackerResponseDTO createTracker(CreateStoryTrackerRequestDTO createStoryTrackerRequestDTO);

    StoryTrackerResponseDTO getTrackerById(UUID storyTrackerId);

    StoryTrackerResponseDTO updateTracker(UUID id, CreateStoryTrackerRequestDTO request);

    StoryTrackerResponseDTO patchTracker(UUID storyTrackerId, CreateStoryTrackerRequestDTO request);

    @Nullable String deleteSubStory(StoryDeleteRequestDTO request);

    @Nullable String deleteStory(StoryDeleteRequestDTO request);
}
