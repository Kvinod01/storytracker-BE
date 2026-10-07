package com.storyTracker.storyTracker.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
    @ToString
    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table(name = "story_tracker")
    public class StoryTracker {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID storyTrackerId; // The main container ID

        @Column(length = 255)
        private String title;
        // One-to-Many relationship to hold all the individual stories
        @OneToMany(
                mappedBy = "storyTracker",
                cascade = CascadeType.ALL, // Cascade all operations (save, update, delete)
                orphanRemoval = true,
                fetch = FetchType.EAGER
        )
        @Builder.Default // Lombok's way to ensure the list is initialized if using @Builder
        private List<Story> stories = new ArrayList<>();

        // Helper method to add a story and manage the bidirectional relationship
        public void addStory(Story story) {
            stories.add(story);
            story.setStoryTracker(this);
        }
}
