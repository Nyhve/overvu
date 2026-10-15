package com.overvu.backend.fitness;

public class ExerciseAlreadyExistsException extends RuntimeException {
    public ExerciseAlreadyExistsException(String name) {
        super("Exercise already exists with name: " + name);
    }
}