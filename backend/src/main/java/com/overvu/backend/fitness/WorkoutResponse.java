package com.overvu.backend.fitness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WorkoutResponse(
        Long id,
        String name,
        LocalDate date,
        String notes,
        boolean completed,
        List<WorkoutExerciseResponse> exercises,
        LocalDateTime createdAt
) {
    public static WorkoutResponse fromEntity(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getName(),
                workout.getDate(),
                workout.getNotes(),
                workout.isCompleted(),
                workout.getExercises().stream()
                        .map(WorkoutExerciseResponse::fromEntity)
                        .toList(),
                workout.getCreatedAt()
        );
    }
}