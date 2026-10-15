package com.overvu.backend.fitness;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AddSetRequest(
        @NotNull(message = "Weight must not be null")
        @DecimalMin(value = "0.0", message = "Weight must be positive")
        BigDecimal weightKg,

        @Min(value = 1, message = "Reps must be at least 1")
        int reps
) {
}