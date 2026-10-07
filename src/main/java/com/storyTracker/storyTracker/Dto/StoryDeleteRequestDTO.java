package com.storyTracker.storyTracker.Dto;

import lombok.Data;

import java.util.UUID;

@Data
public class StoryDeleteRequestDTO {
    private UUID storyTrackerId;
    private UUID storyId;
}
