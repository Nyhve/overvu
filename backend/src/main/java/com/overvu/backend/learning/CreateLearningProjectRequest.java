package com.overvu.backend.learning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLearningProjectRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String description,

        @NotBlank(message = "Topic must not be blank")
        @Size(max = 255, message = "Topic must not exceed 255 characters")
        String topic
) {
}