package com.storyTracker.storyTracker.Entity;

import com.storyTracker.storyTracker.ENUM.Priority;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "story")
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 255)
    private String acName;
    @Column(columnDefinition = "TEXT")
    private  String description;
    private String status ; // Default to false
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_tracker_id", nullable = false)
    private StoryTracker storyTracker;
}
