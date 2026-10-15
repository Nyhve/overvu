package com.overvu.backend.fitness;

import java.util.List;

public record WorkoutExerciseResponse(
        Long id,
        ExerciseResponse exercise,
        int orderIndex,
        List<WorkoutSetResponse> sets
) {
        public static WorkoutExerciseResponse fromEntity(WorkoutExercise workoutExercise) {
        return new WorkoutExerciseResponse(
                workoutExercise.getId(),
                ExerciseResponse.fromEntity(workoutExercise.getExercise()),
                workoutExercise.getOrderIndex(),
                workoutExercise.getSets().stream()
                        .map(WorkoutSetResponse::fromEntity)
                        .toList()
        );
    }
}