package com.overvu.backend.habit;

public class HabitAlreadyCompletedTodayException extends RuntimeException {

    public HabitAlreadyCompletedTodayException() {
        super("Habit already completed today");
    }
}