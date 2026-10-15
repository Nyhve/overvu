package com.overvu.backend.habit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitEntryRepository extends JpaRepository<HabitEntry, Long> {

    List<HabitEntry> findAllByHabitId(Long habitId);

    List<HabitEntry> findAllByHabitUserIdAndDate(Long userId, LocalDate date);

    Optional<HabitEntry> findByHabitIdAndDate(Long habitId, LocalDate date);

    boolean existsByHabitIdAndDate(Long habitId, LocalDate date);
}