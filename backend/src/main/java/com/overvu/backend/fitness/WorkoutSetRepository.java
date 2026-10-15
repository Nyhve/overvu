package com.overvu.backend.fitness;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {
    Optional<WorkoutSet> findByIdAndWorkoutExerciseId(Long id, Long workoutExerciseId);
}