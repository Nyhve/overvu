package com.overvu.backend.habit;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HabitEntryResponse(
        Long id,
        Long habitId,
        LocalDate date,
        LocalDateTime createdAt
) {
    public static HabitEntryResponse fromEntity(HabitEntry entry) {
        return new HabitEntryResponse(
                entry.getId(),
                entry.getHabit().getId(),
                entry.getDate(),
                entry.getCreatedAt()
        );
    }
}