package com.storyTracker.storyTracker.Service.Imp;

import com.storyTracker.storyTracker.Dto.CreateStoryTrackerRequestDTO;
import com.storyTracker.storyTracker.Dto.StoryDeleteRequestDTO;
import com.storyTracker.storyTracker.Dto.StoryItemDTO;
import com.storyTracker.storyTracker.Dto.StoryTrackerResponseDTO;
import com.storyTracker.storyTracker.ENUM.Priority;
import com.storyTracker.storyTracker.Entity.Story;
import com.storyTracker.storyTracker.Entity.StoryTracker;
import com.storyTracker.storyTracker.Repository.StoryRepository;
import com.storyTracker.storyTracker.Repository.StoryTrackerRepository;
import com.storyTracker.storyTracker.Service.StoryTrackerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
public class StoryTrackerServiceImplementation implements StoryTrackerService {

    private final StoryTrackerRepository repository;
    private final StoryRepository storyRepository;
    private final ModelMapper modelMapper;
    @Transactional
    public StoryTrackerResponseDTO createTracker(CreateStoryTrackerRequestDTO createStoryTrackerRequestDTO) {

        // 1. Initialize the main Entity
        StoryTracker newTracker = StoryTracker.builder()
                .title(createStoryTrackerRequestDTO.getStoryTitle())
                .build();

        // 2. Iterate, Convert DTO -> Entity, and Link (Ensuring Defaults)
        for (StoryItemDTO dto : createStoryTrackerRequestDTO.getAcceptanceCriteria()) {

            // Map the DTO to a Story Entity
            Story story = modelMapper.map(dto, Story.class);

            story.setId(null); // Ensure ID is null for new DB entry

            if (story.getPriority() == null) {
                story.setPriority(Priority.LOW);
            }
            // Link the Story to the StoryTracker (using the helper method from your entity)
            newTracker.addStory(story);
        }

        // 3. Save the entity (cascades save to stories)
        StoryTracker savedTracker = repository.save(newTracker);

        // -------------------------------------------------------------------------
        // 4. ROBUST RESPONSE MAPPING (The definitive fix for null data)
        // -------------------------------------------------------------------------

        // a. Map the simple fields (ID, title, etc.)
        StoryTrackerResponseDTO response = modelMapper.map(savedTracker, StoryTrackerResponseDTO.class);

        // b. Manually map the collection of stories using the stream API
        // This guarantees the conversion from List<Story> to List<StoryItemDTO>
        List<StoryItemDTO> storyDTOs = savedTracker.getStories().stream()
                .map(story -> modelMapper.map(story, StoryItemDTO.class))
                .collect(Collectors.toList());

        // c. Explicitly set the converted list into the 'data' field
        response.setAcceptanceCriteria(storyDTOs);

        // d. Set the final message
        response.setMessageText("Created successfully");

        return response;
    }

    @Override
    public StoryTrackerResponseDTO getTrackerById(UUID storyTrackerId) {
        StoryTracker tracker = repository.findById(storyTrackerId)
                .orElseThrow(() -> new RuntimeException("StoryTracker with ID " + storyTrackerId + " not found."));
        // 1. Fetch Priority Counts directly from the DB
        Map<Priority, Long> counts = storyRepository.countStoriesByPriorityForTracker(storyTrackerId).stream()
                .collect(Collectors.toMap(
                        row -> (Priority) row[0],  // Key is the Priority enum
                        row -> (Long) row[1]       // Value is the Count
                ));

        Long totalWeight = storyRepository.calculateTotalWeight(storyTrackerId);
        Long completedWeight = storyRepository.calculateCompletedWeight(storyTrackerId);

        double completionPercentage = 0.0;

        if (totalWeight != null && totalWeight > 0) {
            // Ensure completedWeight is not null (default to 0 if no stories are checked)
            long actualCompletedWeight = completedWeight != null ? completedWeight : 0L;

            // Calculate (Completed Weight / Total Weight) * 100
            completionPercentage = ((double) actualCompletedWeight / totalWeight) * 100.0;
        }

        StoryTrackerResponseDTO response = modelMapper.map(tracker, StoryTrackerResponseDTO.class);

        // 3. Manually map the collection of stories (List<Story> to List<StoryItemDTO>)
        List<StoryItemDTO> storyDTOs = tracker.getStories().stream()
                .map(story -> modelMapper.map(story, StoryItemDTO.class))
                .collect(Collectors.toList());

        // 4. Explicitly set the converted list into the 'data' field
        response.setAcceptanceCriteria(storyDTOs);
        response.setLowPriorityCount(counts.getOrDefault(Priority.LOW, 0L).intValue());
        response.setMediumPriorityCount(counts.getOrDefault(Priority.MEDIUM, 0L).intValue());
        response.setHighPriorityCount(counts.getOrDefault(Priority.HIGH, 0L).intValue());
        response.setCompletionPercentage(Math.round(completionPercentage * 10.0) / 10.0); // Round to one decimal place
        response.setMessageText("Tracker fetched successfully");

        return response;
    }

    @Transactional
    public StoryTrackerResponseDTO updateTracker(UUID trackerId, CreateStoryTrackerRequestDTO request) {
            // 1. Find the existing tracker
            StoryTracker existingTracker = repository.findById(trackerId)
                    .orElseThrow(() -> new RuntimeException("StoryTracker with ID " + trackerId + " not found for update."));

            // 2. Update the main field: title
            existingTracker.setTitle(request.getStoryTitle());

            // 3. Prepare existing stories for fast lookup
            Map<UUID, Story> existingStoriesMap = existingTracker.getStories().stream()
                    .collect(Collectors.toMap(Story::getId, story -> story));

            // Create a list of IDs from the incoming DTO to identify stories to keep
            List<UUID> incomingStoryIds = request.getAcceptanceCriteria().stream()
                    .filter(dto -> dto.getId() != null)
                    .map(StoryItemDTO::getId)
                    .collect(Collectors.toList());

            // --- DELETION: Identify and Remove Stories Missing from the New List ---
            // This relies on orphanRemoval=true in StoryTracker entity, but we must manually
            // remove the children from the collection to trigger it.
            existingTracker.getStories().removeIf(story ->
                    !incomingStoryIds.contains(story.getId())
            );

            // --- ADD/UPDATE: Process incoming DTO items ---
            for (StoryItemDTO dto : request.getAcceptanceCriteria()) {

                UUID dtoId = dto.getId();

                if (dtoId == null) {
                    // A. ADDITION: ID is null, so create a new Story entity
                    Story newStory = modelMapper.map(dto, Story.class);
                    newStory.setId(null); // Ensure ID is null for new insert

                    // Set defaults if necessary (crucial for clean data)
                    if (newStory.getPriority() == null) { newStory.setPriority(Priority.LOW); }

                    existingTracker.addStory(newStory); // Uses helper to link back

                } else {
                    // B. UPDATE: ID is present, find and update the existing entity
                    Story storyToUpdate = existingStoriesMap.get(dtoId);

                    if (storyToUpdate != null) {
                        // Update fields from DTO to Entity
                        storyToUpdate.setAcName(dto.getAcName());
                        storyToUpdate.setDescription(dto.getDescription());
                        storyToUpdate.setStatus(dto.getStatus());

                        // Only update if DTO value is not null, to allow partial updates
                        if (dto.getPriority() != null) {
                            storyToUpdate.setPriority(dto.getPriority());
                        }

                    }
                }
            }

            // 4. Save the tracker (cascades changes to stories, including deletions)
            StoryTracker savedTracker = repository.save(existingTracker);

            // 5. Build the response (using our reliable two-step mapping)
            StoryTrackerResponseDTO response = modelMapper.map(savedTracker, StoryTrackerResponseDTO.class);

            List<StoryItemDTO> storyDTOs = savedTracker.getStories().stream()
                    .map(story -> modelMapper.map(story, StoryItemDTO.class))
                    .collect(Collectors.toList());
            response.setAcceptanceCriteria(storyDTOs);

            response.setMessageText("Tracker updated successfully");
            // We typically don't include priority counts in the update response,
            // but you can add the calculation here if needed.

            return response;
        }

    @Override
    public StoryTrackerResponseDTO patchTracker(UUID trackerId, CreateStoryTrackerRequestDTO request) {
        StoryTracker existingTracker = repository.findById(trackerId)
                .orElseThrow(() -> new RuntimeException("StoryTracker with ID " + trackerId + " not found for update."));
        if (request.getStoryTitle() != null) {
            existingTracker.setTitle(request.getStoryTitle());
        }

        if (request.getAcceptanceCriteria() != null && !request.getAcceptanceCriteria().isEmpty()) {

            // Prepare existing stories for fast lookup
            Map<UUID, Story> existingStoriesMap = existingTracker.getStories().stream()
                    .collect(Collectors.toMap(Story::getId, story -> story));

            // **CRUCIAL CHANGE: WE SKIP THE DELETION LOGIC.**
            // Only Add and Update logic remains.

            for (StoryItemDTO dto : request.getAcceptanceCriteria()) {

                UUID dtoId = dto.getId();

                if (dtoId == null) {
                    // A. ADDITION: ID is null, create a new Story entity
                    Story newStory = modelMapper.map(dto, Story.class);
                    newStory.setId(null);
                    // Set defaults...

                    existingTracker.addStory(newStory);

                } else {
                    // B. UPDATE: ID is present, find and update the existing entity
                    Story storyToUpdate = existingStoriesMap.get(dtoId);

                    if (storyToUpdate != null) {
                        // Apply partial updates to the fields
                        if (dto.getAcName() != null) { storyToUpdate.setAcName(dto.getAcName()); }
                        if (dto.getDescription() != null) { storyToUpdate.setDescription(dto.getDescription()); }
                        if (dto.getPriority() != null) { storyToUpdate.setPriority(dto.getPriority()); }
                        if (dto.getStatus() != null) { storyToUpdate.setStatus(dto.getStatus()); }
                    }
                    // Note: If ID is not found, we ignore the DTO.
                }
            }
        }
        // --------------------------------------------------------------------------

        // 4. Save the tracker (cascades changes)
        StoryTracker savedTracker = repository.save(existingTracker);

        // 5. Build the response (using our reliable two-step mapping)
        // ... (same response mapping logic as before)

        StoryTrackerResponseDTO response = modelMapper.map(savedTracker, StoryTrackerResponseDTO.class);
        // ... manual list mapping and message setting ...

        return response;
    }

    @Transactional
    public @Nullable String deleteSubStory(StoryDeleteRequestDTO request) {
        int rowsDeleted = storyRepository.deleteStoryByIdAndTrackerId(
                request.getStoryId(),
                request.getStoryTrackerId()
        );
        if (rowsDeleted == 0) {
            throw new RuntimeException(
                    "Story with ID " + request.getStoryId() +
                            " not found or does not belong to Tracker " + request.getStoryTrackerId()
            );
        }
        return "Sub story deleted successfully";
    }

    @Override
    public @Nullable String deleteStory(StoryDeleteRequestDTO request) {
        if (!repository.existsById(request.getStoryTrackerId())) {
            throw new RuntimeException("StoryTracker with ID " + request.getStoryTrackerId() + " not found for deletion.");
        }
        repository.deleteById(request.getStoryTrackerId());
        return "Story deleted successfully";
    }
}
