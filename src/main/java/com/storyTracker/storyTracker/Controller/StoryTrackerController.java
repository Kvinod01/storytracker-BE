package com.storyTracker.storyTracker.Controller;

import com.storyTracker.storyTracker.Dto.CreateStoryTrackerRequestDTO;
import com.storyTracker.storyTracker.Dto.StoryDeleteRequestDTO;
import com.storyTracker.storyTracker.Dto.StoryTrackerResponseDTO;
import com.storyTracker.storyTracker.Service.StoryTrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/story-trackers")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class StoryTrackerController {
    private final StoryTrackerService storyTrackerService;

    @PostMapping
    public ResponseEntity<StoryTrackerResponseDTO> createStoryTracker (@RequestBody CreateStoryTrackerRequestDTO createStoryTrackerRequestDTO){
        StoryTrackerResponseDTO response = storyTrackerService.createTracker(createStoryTrackerRequestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{storyTrackerId}")
    public ResponseEntity<StoryTrackerResponseDTO> getStoryTrackerById(
            @PathVariable UUID storyTrackerId) {

        StoryTrackerResponseDTO response = storyTrackerService.getTrackerById(storyTrackerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping()
    public ResponseEntity<StoryTrackerResponseDTO> updateTracker(
            @RequestBody CreateStoryTrackerRequestDTO request) {

        StoryTrackerResponseDTO response = storyTrackerService.updateTracker(request.getStoryTrackerId(), request);
        return ResponseEntity.ok(response);
    }
    @PatchMapping()
    public ResponseEntity<StoryTrackerResponseDTO> patchTracker(
            @RequestBody CreateStoryTrackerRequestDTO request) {

        StoryTrackerResponseDTO response = storyTrackerService.patchTracker(request.getStoryTrackerId(), request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/delete") // Path indicates a collection of stories is being modified
    public ResponseEntity<String> deleteSubStory(@RequestBody StoryDeleteRequestDTO request) {
        // Standard response for a successful DELETE is 204 No Content
        return ResponseEntity.ok(storyTrackerService.deleteSubStory(request));
    }
    @DeleteMapping("/delete/story") // Path indicates a collection of stories is being modified
    public ResponseEntity<String> deleteStory(@RequestBody StoryDeleteRequestDTO request) {
        // Standard response for a successful DELETE is 204 No Content
        return ResponseEntity.ok(storyTrackerService.deleteStory(request));
    }
}
