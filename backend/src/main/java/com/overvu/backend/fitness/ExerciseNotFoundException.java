package com.overvu.backend.fitness;

public class ExerciseNotFoundException extends RuntimeException {
    public ExerciseNotFoundException(Long id) {
        super("Exercise not found with id: " + id);
    }
}