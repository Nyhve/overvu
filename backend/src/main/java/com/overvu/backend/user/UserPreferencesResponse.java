package com.overvu.backend.user;

public record UserPreferencesResponse(
        boolean goalsEnabled,
        boolean tasksEnabled,
        boolean habitsEnabled,
        boolean fitnessEnabled,
        boolean learningEnabled,
        boolean financeEnabled
) {
    public static UserPreferencesResponse fromEntity(UserPreferences prefs) {
        return new UserPreferencesResponse(
                prefs.isGoalsEnabled(),
                prefs.isTasksEnabled(),
                prefs.isHabitsEnabled(),
                prefs.isFitnessEnabled(),
                prefs.isLearningEnabled(),
                prefs.isFinanceEnabled()
        );
    }
}