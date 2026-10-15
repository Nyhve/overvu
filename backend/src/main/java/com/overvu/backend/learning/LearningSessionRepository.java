package com.overvu.backend.learning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
    List<LearningSession> findAllByProjectId(Long projectId);
    List<LearningSession> findAllByProjectUserId(Long userId);
}