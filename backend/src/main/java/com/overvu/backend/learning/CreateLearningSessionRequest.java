package com.overvu.backend.learning;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateLearningSessionRequest(
        @Min(value = 1, message = "Duration must be at least 1 minute")
        int durationMinutes,

        String notes,

        @NotNull(message = "Date must not be null")
        LocalDate date
) {
}