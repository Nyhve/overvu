package com.overvu.backend.learning;

import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.activity.ActivityType;
import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningService {

    private final LearningProjectRepository projectRepository;
    private final LearningSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ActivityService activityService;

    public LearningService(LearningProjectRepository projectRepository,
                            LearningSessionRepository sessionRepository,
                            UserRepository userRepository,
                            CurrentUserProvider currentUserProvider,
                            ActivityService activityService) {
        this.projectRepository = projectRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.activityService = activityService;
    }

    public List<LearningProjectResponse> getAllProjects() {
        Long userId = currentUserProvider.getCurrentUserId();
        return projectRepository.findAllByUserId(userId).stream()
                .map(project -> {
                    int total = calculateTotalMinutes(project.getId());
                    return LearningProjectResponse.fromEntity(project, total);
                })
                .toList();
    }

    public LearningProjectResponse createProject(CreateLearningProjectRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);
        LearningProject project = new LearningProject(
                user, request.title(), request.description(), request.topic()
        );
        LearningProject saved = projectRepository.save(project);
        return LearningProjectResponse.fromEntity(saved, 0);
    }

    public void deleteProject(Long id) {
        LearningProject project = findProjectOrThrow(id);
        projectRepository.delete(project);
    }

    public LearningSessionResponse addSession(Long projectId, CreateLearningSessionRequest request) {
        LearningProject project = findProjectOrThrow(projectId);
        LearningSession session = new LearningSession(
                project,
                request.durationMinutes(),
                request.notes(),
                request.date()
        );
        LearningSession saved = sessionRepository.save(session);
        activityService.record(ActivityType.LEARNING_SESSION_COMPLETED, projectId);
        return LearningSessionResponse.fromEntity(saved);
    }

    public List<LearningSessionResponse> getSessions(Long projectId) {
        findProjectOrThrow(projectId);
        return sessionRepository.findAllByProjectId(projectId).stream()
                .map(LearningSessionResponse::fromEntity)
                .toList();
    }

    public void deleteSession(Long projectId, Long sessionId) {
        findProjectOrThrow(projectId);
        LearningSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new LearningProjectNotFoundException(sessionId));
        sessionRepository.delete(session);
    }

    private int calculateTotalMinutes(Long projectId) {
        return sessionRepository.findAllByProjectId(projectId).stream()
                .mapToInt(LearningSession::getDurationMinutes)
                .sum();
    }

    private LearningProject findProjectOrThrow(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new LearningProjectNotFoundException(id));
    }
}