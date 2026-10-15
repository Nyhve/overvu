package com.overvu.backend.habit;

import java.time.LocalDateTime;

public record HabitResponse(
        Long id,
        String name,
        String description,
        HabitFrequency frequency,
        LocalDateTime createdAt
) {
    public static HabitResponse fromEntity(Habit habit) {
        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.getFrequency(),
                habit.getCreatedAt()
        );
    }
}