package com.overvu.backend.learning;

import java.time.LocalDateTime;

public record LearningProjectResponse(
        Long id,
        String title,
        String description,
        String topic,
        int totalMinutes,
        LocalDateTime createdAt
) {
    public static LearningProjectResponse fromEntity(LearningProject project, int totalMinutes) {
        return new LearningProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getTopic(),
                totalMinutes,
                project.getCreatedAt()
        );
    }
}