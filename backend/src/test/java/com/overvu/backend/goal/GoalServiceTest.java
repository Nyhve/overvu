package com.overvu.backend.goal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.activity.ActivityType;
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
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private GoalService goalService;

    private final Long userId = 1L;
    private final User user = new User("test@example.com", "hashedValue");

    @Test
    void createGoal_setsStatusToActive() {
        CreateGoalRequest request = new CreateGoalRequest("Neues Ziel", "Beschreibung", LocalDate.of(2026, 12, 31));
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GoalResponse response = goalService.createGoal(request);
        assertThat(response.status()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(response.title()).isEqualTo("Neues Ziel");
        verify(goalRepository).save(any(Goal.class));
        verify(activityService).record(eq(ActivityType.GOAL_CREATED), any());
    }

    @Test
    void getGoalById_returnsGoal_whenGoalExists() {
        Goal goal = new Goal("Bestehendes Ziel", "Beschreibung", LocalDate.of(2026, 6, 1), GoalStatus.ACTIVE, user);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(goalRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(goal));
        GoalResponse response = goalService.getGoalById(1L);
        assertThat(response.title()).isEqualTo("Bestehendes Ziel");
    }

    @Test
    void getGoalById_throwsException_whenGoalDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(goalRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> goalService.getGoalById(999L))
                .isInstanceOf(GoalNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateGoal_updatesAllFields() {
        Goal existingGoal = new Goal("Alter Titel", "Alte Beschreibung", LocalDate.of(2026, 1, 1), GoalStatus.ACTIVE, user);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(goalRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateGoalRequest request = new UpdateGoalRequest(
                "Neuer Titel", "Neue Beschreibung", LocalDate.of(2026, 12, 31), GoalStatus.COMPLETED
        );
        GoalResponse response = goalService.updateGoal(1L, request);

        assertThat(response.title()).isEqualTo("Neuer Titel");
        assertThat(response.status()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void deleteGoal_throwsException_whenGoalDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(goalRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.deleteGoal(999L))
                .isInstanceOf(GoalNotFoundException.class);

        verify(goalRepository, never()).delete(any(Goal.class));
    }

    @Test
    void getAllGoals_returnsEmptyList_whenNoGoalsExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(goalRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<GoalResponse> responses = goalService.getAllGoals();

        assertThat(responses).isEmpty();
    }
}