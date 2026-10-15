package com.overvu.backend.habit;

public record HabitConsistencyResponse(
        Long habitId,
        String name,
        HabitFrequency frequency,
        int totalDays,
        int completedDays,
        double consistencyPercent
) {
}