package com.overvu.backend.learning;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LearningSessionResponse(
        Long id,
        Long projectId,
        int durationMinutes,
        String notes,
        LocalDate date,
        LocalDateTime createdAt
) {
    public static LearningSessionResponse fromEntity(LearningSession session) {
        return new LearningSessionResponse(
                session.getId(),
                session.getProject().getId(),
                session.getDurationMinutes(),
                session.getNotes(),
                session.getDate(),
                session.getCreatedAt()
        );
    }
}