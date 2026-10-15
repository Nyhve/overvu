package com.overvu.backend.habit;

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
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitEntryRepository habitEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private HabitService habitService;

    private final Long userId = 1L;
    private final User user = new User("test@example.com", "hashedValue");

    @Test
    void createHabit_savesAndReturnsHabit() {
        CreateHabitRequest request = new CreateHabitRequest("Meditieren", "10 Minuten", HabitFrequency.DAILY);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HabitResponse response = habitService.createHabit(request);

        assertThat(response.name()).isEqualTo("Meditieren");
        assertThat(response.frequency()).isEqualTo(HabitFrequency.DAILY);
        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void completeToday_savesEntryAndRecordsActivity() {
        Habit habit = new Habit(user, "Meditieren", null, HabitFrequency.DAILY);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(habitRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(habit));
        when(habitEntryRepository.existsByHabitIdAndDate(any(), any())).thenReturn(false);
        when(habitEntryRepository.save(any(HabitEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        habitService.completeToday(1L);

        verify(habitEntryRepository).save(any(HabitEntry.class));
        verify(activityService).record(eq(ActivityType.HABIT_COMPLETED), any());
    }

    @Test
    void completeToday_throwsException_whenAlreadyCompletedToday() {
        Habit habit = new Habit(user, "Meditieren", null, HabitFrequency.DAILY);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(habitRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(habit));
        when(habitEntryRepository.existsByHabitIdAndDate(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> habitService.completeToday(1L))
                .isInstanceOf(HabitAlreadyCompletedTodayException.class);

        verify(habitEntryRepository, never()).save(any());
        verify(activityService, never()).record(any(), any());
    }

    @Test
    void deleteHabit_throwsException_whenHabitDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(habitRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.deleteHabit(999L))
                .isInstanceOf(HabitNotFoundException.class);

        verify(habitRepository, never()).delete(any());
    }

    @Test
    void getAllHabits_returnsEmptyList_whenNoHabitsExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(habitRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<HabitResponse> result = habitService.getAllHabits();

        assertThat(result).isEmpty();
    }
}