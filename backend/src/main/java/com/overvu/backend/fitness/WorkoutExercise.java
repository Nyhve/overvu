package com.overvu.backend.fitness;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

@Entity
@Table(name = "workout_exercises")
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "order_index")
    private int orderIndex;

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private Set<WorkoutSet> sets = new LinkedHashSet<>();

    protected WorkoutExercise() {
    }

    public WorkoutExercise(Workout workout, Exercise exercise, int orderIndex) {
        this.workout = workout;
        this.exercise = exercise;
        this.orderIndex = orderIndex;
    }

    public Long getId() { return id; }
    public Workout getWorkout() { return workout; }
    public Exercise getExercise() { return exercise; }
    public int getOrderIndex() { return orderIndex; }
    public Set<WorkoutSet> getSets() { return sets; }
}