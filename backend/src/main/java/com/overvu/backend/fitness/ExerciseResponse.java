package com.overvu.backend.fitness;

public record ExerciseResponse(
        Long id,
        String name,
        MuscleGroup muscleGroup
) {
    public static ExerciseResponse fromEntity(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getMuscleGroup()
        );
    }
}