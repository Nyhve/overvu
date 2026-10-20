package com.overvu.backend.user;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "goals_enabled", nullable = false)
    private boolean goalsEnabled = true;

    @Column(name = "tasks_enabled", nullable = false)
    private boolean tasksEnabled = true;

    @Column(name = "habits_enabled", nullable = false)
    private boolean habitsEnabled = true;

    @Column(name = "fitness_enabled", nullable = false)
    private boolean fitnessEnabled = true;

    @Column(name = "learning_enabled", nullable = false)
    private boolean learningEnabled = true;

    @Column(name = "finance_enabled", nullable = false)
    private boolean financeEnabled = true;

    protected UserPreferences() {
    }

    public UserPreferences(User user) {
        this.user = user;
    }

    public Long getId() { return id; }
    public boolean isGoalsEnabled() { return goalsEnabled; }
    public boolean isTasksEnabled() { return tasksEnabled; }
    public boolean isHabitsEnabled() { return habitsEnabled; }
    public boolean isFitnessEnabled() { return fitnessEnabled; }
    public boolean isLearningEnabled() { return learningEnabled; }
    public boolean isFinanceEnabled() { return financeEnabled; }

    public void setGoalsEnabled(boolean v) { this.goalsEnabled = v; }
    public void setTasksEnabled(boolean v) { this.tasksEnabled = v; }
    public void setHabitsEnabled(boolean v) { this.habitsEnabled = v; }
    public void setFitnessEnabled(boolean v) { this.fitnessEnabled = v; }
    public void setLearningEnabled(boolean v) { this.learningEnabled = v; }
    public void setFinanceEnabled(boolean v) { this.financeEnabled = v; }
}