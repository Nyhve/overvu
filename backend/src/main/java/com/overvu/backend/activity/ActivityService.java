package com.overvu.backend.activity;

import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           CurrentUserProvider currentUserProvider) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public void record(ActivityType type, Long referenceId) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);
        Activity activity = new Activity(user, type, referenceId);
        activityRepository.save(activity);
    }

    public List<ActivityResponse> getActivityFeed() {
        Long userId = currentUserProvider.getCurrentUserId();
        return activityRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ActivityResponse::fromEntity)
                .toList();
    }

    public List<DailyActivityCount> getDailyActivityCounts(int days) {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDateTime from = LocalDateTime.now().minusDays(days);

        List<Activity> activities = activityRepository.findAllByUserIdSince(userId, from);

        Map<LocalDate, Long> countByDate = activities.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        return countByDate.entrySet().stream()
                .map(e -> new DailyActivityCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.date().compareTo(b.date()))
                .toList();
    }
}