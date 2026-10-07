package com.storyTracker.storyTracker.Dto;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateStoryTrackerRequestDTO {
    private UUID storyTrackerId;
    @Column(length = 255)
    private String storyTitle;
    private List<StoryItemDTO> acceptanceCriteria;
    }

