package com.overvu.backend.habit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findAllByUserId(Long userId);

    Optional<Habit> findByIdAndUserId(Long id, Long userId);
}