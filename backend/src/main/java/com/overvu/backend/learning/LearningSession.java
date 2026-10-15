package com.overvu.backend.learning;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_sessions")
public class LearningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_project_id", nullable = false)
    private LearningProject project;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LearningSession() {
    }

    public LearningSession(LearningProject project, int durationMinutes, String notes, LocalDate date) {
        this.project = project;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
        this.date = date;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public LearningProject getProject() { return project; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getNotes() { return notes; }
    public LocalDate getDate() { return date; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}