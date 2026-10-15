package com.overvu.backend.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT a FROM Activity a WHERE a.user.id = :userId " +
        "AND a.createdAt >= :from ORDER BY a.createdAt DESC")
    List<Activity> findAllByUserIdSince(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from
    );
}