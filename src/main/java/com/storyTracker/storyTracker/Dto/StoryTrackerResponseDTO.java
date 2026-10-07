package com.storyTracker.storyTracker.Dto;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryTrackerResponseDTO {
    private String messageText;
    private UUID storyTrackerId;
    private List<StoryItemDTO> acceptanceCriteria;
    private String storyTitle;
    private Integer lowPriorityCount;
    private Integer mediumPriorityCount;
    private Integer highPriorityCount;
    private Double completionPercentage;
    // Optional fields for paginated view (GET ALL)
//    private Integer page;
//    private Integer size;
}
