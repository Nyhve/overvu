package com.overvu.backend.learning;

public class LearningProjectNotFoundException extends RuntimeException {
    public LearningProjectNotFoundException(Long id) {
        super("Learning project not found with id: " + id);
    }
}