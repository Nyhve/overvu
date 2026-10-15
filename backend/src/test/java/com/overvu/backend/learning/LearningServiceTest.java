package com.overvu.backend.learning;

import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.activity.ActivityType;
import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningServiceTest {

    @Mock
    private LearningProjectRepository projectRepository;

    @Mock
    private LearningSessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private LearningService learningService;

    private final Long userId = 1L;
    private final User user = new User("test@example.com", "hashedValue");

    @Test
    void createProject_savesAndReturnsProject() {
        CreateLearningProjectRequest request = new CreateLearningProjectRequest(
                "Spring Boot", "Backend lernen", "Java"
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(projectRepository.save(any(LearningProject.class)))
                .thenAnswer(i -> i.getArgument(0));

        LearningProjectResponse response = learningService.createProject(request);

        assertThat(response.title()).isEqualTo("Spring Boot");
        assertThat(response.topic()).isEqualTo("Java");
        assertThat(response.totalMinutes()).isEqualTo(0);
        verify(projectRepository).save(any(LearningProject.class));
    }

    @Test
    void addSession_savesSessionAndRecordsActivity() {
        LearningProject project = new LearningProject(user, "Spring Boot", null, "Java");
        CreateLearningSessionRequest request = new CreateLearningSessionRequest(
                45, "Lazy Loading", LocalDate.now()
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(projectRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(project));
        when(sessionRepository.save(any(LearningSession.class)))
                .thenAnswer(i -> i.getArgument(0));

        learningService.addSession(1L, request);

        verify(sessionRepository).save(any(LearningSession.class));
        verify(activityService).record(eq(ActivityType.LEARNING_SESSION_COMPLETED), any());
    }

    @Test
    void deleteProject_throwsException_whenProjectDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(projectRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> learningService.deleteProject(999L))
                .isInstanceOf(LearningProjectNotFoundException.class);

        verify(projectRepository, never()).delete(any());
    }

    @Test
    void getAllProjects_returnsEmptyList_whenNoProjectsExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(projectRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<LearningProjectResponse> result = learningService.getAllProjects();

        assertThat(result).isEmpty();
    }

    @Test
    void getSessions_returnsSessionsForProject() {
        LearningProject project = new LearningProject(user, "Spring Boot", null, "Java");
        LearningSession session = new LearningSession(project, 60, "Notes", LocalDate.now());
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(projectRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(project));
        when(sessionRepository.findAllByProjectId(1L)).thenReturn(List.of(session));

        List<LearningSessionResponse> result = learningService.getSessions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).durationMinutes()).isEqualTo(60);
    }
}