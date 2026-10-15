package com.overvu.backend.learning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningProjectRepository extends JpaRepository<LearningProject, Long> {
    List<LearningProject> findAllByUserId(Long userId);
    Optional<LearningProject> findByIdAndUserId(Long id, Long userId);
}