package com.storyTracker.storyTracker.Constants;

import com.storyTracker.storyTracker.ENUM.Priority;

import java.util.Map;

public class PriorityWeights {
        public static final int LOW_WEIGHT = 1;
        public static final int MEDIUM_WEIGHT = 3;
        public static final int HIGH_WEIGHT = 5;

        // Static mapping for easy lookup by Priority enum
        private static final Map<Priority, Integer> WEIGHT_MAP = Map.of(
                Priority.LOW, LOW_WEIGHT,
                Priority.MEDIUM, MEDIUM_WEIGHT,
                Priority.HIGH, HIGH_WEIGHT
        );

        /**
         * Gets the weight value for a given Priority.
         */
        public static int getWeight(Priority priority) {
            return WEIGHT_MAP.getOrDefault(priority, 0);
        }
}
