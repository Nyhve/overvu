package com.overvu.backend.activity;

import java.time.LocalDateTime;

public record ActivityResponse(
        Long id,
        ActivityType type,
        Long referenceId,
        LocalDateTime createdAt
) {
    public static ActivityResponse fromEntity(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getType(),
                activity.getReferenceId(),
                activity.getCreatedAt()
        );
    }
}