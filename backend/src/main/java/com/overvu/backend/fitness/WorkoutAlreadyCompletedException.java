package com.overvu.backend.fitness;

public class WorkoutAlreadyCompletedException extends RuntimeException {
    public WorkoutAlreadyCompletedException() {
        super("Workout is already completed and cannot be modified");
    }
}