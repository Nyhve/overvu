package com.overvu.backend.user;

public record UpdateUserPreferencesRequest(
        boolean goalsEnabled,
        boolean tasksEnabled,
        boolean habitsEnabled,
        boolean fitnessEnabled,
        boolean learningEnabled,
        boolean financeEnabled
) {
}