package com.overvu.backend.activity;

import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ActivityService activityService;

    private final Long userId = 1L;
    private final User user = new User("test@example.com", "hashedValue");

    @Test
    void record_savesActivityWithCorrectTypeAndReference() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(activityRepository.save(any(Activity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        activityService.record(ActivityType.GOAL_CREATED, 5L);

        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void getActivityFeed_returnsActivitiesForCurrentUser() {
        Activity activity = new Activity(user, ActivityType.TASK_COMPLETED, 3L);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(activityRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(activity));

        List<ActivityResponse> result = activityService.getActivityFeed();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(ActivityType.TASK_COMPLETED);
        assertThat(result.get(0).referenceId()).isEqualTo(3L);
    }

    @Test
    void getActivityFeed_returnsEmptyList_whenNoActivities() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(activityRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        List<ActivityResponse> result = activityService.getActivityFeed();

        assertThat(result).isEmpty();
    }
}