package com.overvu.backend.fitness;

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
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private WorkoutSetRepository workoutSetRepository;

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private WorkoutService workoutService;

    private final Long userId = 1L;
    private final User user = new User("test@example.com", "hashedValue");

    @Test
    void createWorkout_savesAndReturnsWorkout() {
        CreateWorkoutRequest request = new CreateWorkoutRequest("Push Day", LocalDate.now(), null);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(workoutRepository.save(any(Workout.class))).thenAnswer(i -> i.getArgument(0));

        WorkoutResponse response = workoutService.createWorkout(request);

        assertThat(response.name()).isEqualTo("Push Day");
        assertThat(response.completed()).isFalse();
        verify(workoutRepository).save(any(Workout.class));
    }

    @Test
    void completeWorkout_setsCompletedAndRecordsActivity() {
        Workout workout = new Workout(user, "Push Day", LocalDate.now(), null);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(workoutRepository.findByIdAndUserIdWithExercises(1L, userId))
                .thenReturn(Optional.of(workout));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(i -> i.getArgument(0));

        workoutService.completeWorkout(1L);

        verify(workoutRepository).save(any(Workout.class));
        verify(activityService).record(eq(ActivityType.WORKOUT_COMPLETED), any());
    }

    @Test
    void completeWorkout_throwsException_whenAlreadyCompleted() {
        Workout workout = new Workout(user, "Push Day", LocalDate.now(), null);
        workout.setCompleted(true);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(workoutRepository.findByIdAndUserIdWithExercises(1L, userId))
                .thenReturn(Optional.of(workout));

        assertThatThrownBy(() -> workoutService.completeWorkout(1L))
                .isInstanceOf(WorkoutAlreadyCompletedException.class);

        verify(activityService, never()).record(any(), any());
    }

    @Test
    void deleteWorkout_throwsException_whenWorkoutDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(workoutRepository.findByIdAndUserIdWithExercises(999L, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.deleteWorkout(999L))
                .isInstanceOf(WorkoutNotFoundException.class);

        verify(workoutRepository, never()).delete(any());
    }

    @Test
    void getAllWorkouts_returnsEmptyList_whenNoWorkoutsExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(workoutRepository.findAllByUserIdWithExercises(userId)).thenReturn(List.of());

        List<WorkoutResponse> result = workoutService.getAllWorkouts();

        assertThat(result).isEmpty();
    }
}