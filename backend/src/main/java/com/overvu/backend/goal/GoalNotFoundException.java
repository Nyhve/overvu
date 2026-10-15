package com.overvu.backend.goal;

public class GoalNotFoundException extends RuntimeException {

    public GoalNotFoundException(Long id) {
        super("Goal not found with id: " + id);
    }
}