package com.overvu.backend.fitness;

import jakarta.validation.constraints.NotNull;

public record AddExerciseToWorkoutRequest(
        @NotNull(message = "Exercise ID must not be null")
        Long exerciseId
) {
}