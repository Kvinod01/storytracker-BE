package com.storyTracker.storyTracker.Dto;
import com.storyTracker.storyTracker.ENUM.Priority;
import lombok.*;
import java.util.UUID;

@Data // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryItemDTO {
    private UUID id;
    private String status;
    private String description;
    private String acName;
    private Priority priority; // Use the Enum directly
}