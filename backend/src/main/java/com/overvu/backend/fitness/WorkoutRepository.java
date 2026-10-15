package com.overvu.backend.fitness;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findAllByUserIdOrderByDateDesc(Long userId);
    Optional<Workout> findByIdAndUserId(Long id, Long userId);
    @Query("SELECT w FROM Workout w " +
        "LEFT JOIN FETCH w.exercises we " +
        "LEFT JOIN FETCH we.exercise " +
        "LEFT JOIN FETCH we.sets " +
        "WHERE w.id = :id AND w.user.id = :userId")
    Optional<Workout> findByIdAndUserIdWithExercises(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT w FROM Workout w " +
        "LEFT JOIN FETCH w.exercises we " +
        "LEFT JOIN FETCH we.exercise " +
        "LEFT JOIN FETCH we.sets " +
        "WHERE w.user.id = :userId " +
        "ORDER BY w.date DESC")
    List<Workout> findAllByUserIdWithExercises(@Param("userId") Long userId);
}