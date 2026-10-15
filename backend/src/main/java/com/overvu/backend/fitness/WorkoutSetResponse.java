package com.overvu.backend.fitness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkoutSetResponse(
        Long id,
        int setNumber,
        BigDecimal weightKg,
        int reps,
        LocalDateTime completedAt
) {
    public static WorkoutSetResponse fromEntity(WorkoutSet set) {
        return new WorkoutSetResponse(
                set.getId(),
                set.getSetNumber(),
                set.getWeightKg(),
                set.getReps(),
                set.getCompletedAt()
        );
    }
}