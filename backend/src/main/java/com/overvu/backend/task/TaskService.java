package com.overvu.backend.task;

import org.springframework.stereotype.Service;

import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.goal.GoalRepository;
import com.overvu.backend.goal.GoalResponse;
import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.UserRepository;
import com.overvu.backend.user.User;
import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.activity.ActivityType;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ActivityService activityService;

    public TaskService(TaskRepository goalRepository, UserRepository userRepository,
                        CurrentUserProvider currentUserProvider, ActivityService activityService) {
        this.taskRepository = goalRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.activityService = activityService;
    }

    public List<TaskResponse> getAllTasks() {
        Long userId = currentUserProvider.getCurrentUserId();
        return taskRepository.findAllByUserId(userId).stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        Task task = findTaskOrThrow(id);
        return TaskResponse.fromEntity(task);
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);
        Task task = new Task(request.title(), request.description(), request.dueDate(), user);
        Task savedTask = taskRepository.save(task);
        activityService.record(ActivityType.TASK_CREATED, savedTask.getId());
        return TaskResponse.fromEntity(savedTask);
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task task = findTaskOrThrow(id);
        boolean wasIncomplete = !task.isCompleted();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setCompleted(request.completed());
        Task savedTask = taskRepository.save(task);
        if (wasIncomplete && savedTask.isCompleted()) {
            activityService.record(ActivityType.TASK_COMPLETED, savedTask.getId());
        }
        return TaskResponse.fromEntity(savedTask);
    }

    public void deleteTask(Long id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    private Task findTaskOrThrow(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}