package com.overvu.backend.fitness;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_sets")
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(name = "set_number", nullable = false)
    private int setNumber;

    @Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(nullable = false)
    private int reps;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    protected WorkoutSet() {
    }

    public WorkoutSet(WorkoutExercise workoutExercise, int setNumber, BigDecimal weightKg, int reps) {
        this.workoutExercise = workoutExercise;
        this.setNumber = setNumber;
        this.weightKg = weightKg;
        this.reps = reps;
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public WorkoutExercise getWorkoutExercise() { return workoutExercise; }
    public int getSetNumber() { return setNumber; }
    public BigDecimal getWeightKg() { return weightKg; }
    public int getReps() { return reps; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}