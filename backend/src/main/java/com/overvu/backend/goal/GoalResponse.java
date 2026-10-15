package com.overvu.backend.goal;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalResponse(
        Long id,
        String title,
        String description,
        LocalDate targetDate,
        GoalStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GoalResponse fromEntity(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getTargetDate(),
                goal.getStatus(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}