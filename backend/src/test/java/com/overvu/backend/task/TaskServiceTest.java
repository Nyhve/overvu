package com.overvu.backend.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.UserRepository;
import com.overvu.backend.user.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.overvu.backend.activity.ActivityType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock 
    private ActivityService activityService;

    @InjectMocks
    private TaskService taskService;

    private final Long userId = 1L;
    private final User user = new User("test@example.com", "hashedValue");

    @Test
    void createTask_setsCompletedToFalse() {
        CreateTaskRequest request = new CreateTaskRequest("Neuer Task", "Beschreibung", LocalDate.of(2026, 9, 1));
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TaskResponse response = taskService.createTask(request);
        assertThat(response.completed()).isFalse();
        assertThat(response.title()).isEqualTo("Neuer Task");
        verify(taskRepository).save(any(Task.class));
        verify(activityService).record(eq(ActivityType.TASK_CREATED), any());
    }

    @Test
    void getTaskById_returnsTask_whenTaskExists() {
        Task task = new Task("Bestehender Task", "Beschreibung", LocalDate.of(2026, 6, 1), user);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(taskRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(task));
        TaskResponse response = taskService.getTaskById(1L);
        assertThat(response.title()).isEqualTo("Bestehender Task");
    }

    @Test
    void getTaskById_throwsException_whenTaskDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(taskRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateTask_updatesAllFields() {
        Task existingTask = new Task("Alter Titel", "Alte Beschreibung", LocalDate.of(2026, 1, 1), user);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(taskRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Neuer Titel", "Neue Beschreibung", LocalDate.of(2026, 12, 31), true
        );
        TaskResponse response = taskService.updateTask(1L, request);
        assertThat(response.title()).isEqualTo("Neuer Titel");
        assertThat(response.completed()).isTrue();
    }

    @Test
    void deleteTask_throwsException_whenTaskDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(taskRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.deleteTask(999L))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void getAllTasks_returnsEmptyList_whenNoTasksExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(taskRepository.findAllByUserId(userId)).thenReturn(List.of());
        List<TaskResponse> responses = taskService.getAllTasks();
        assertThat(responses).isEmpty();
    }
}