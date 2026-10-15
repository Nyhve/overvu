package com.overvu.backend.fitness;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    boolean existsByName(String name);
    Optional<Exercise> findByName(String name);
}