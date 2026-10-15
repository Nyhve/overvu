package com.overvu.backend.habit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateHabitRequest(
        @NotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        String description,

        @NotNull(message = "Frequency must not be null")
        HabitFrequency frequency
) {
}