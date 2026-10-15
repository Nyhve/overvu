package com.overvu.backend.goal;

import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.activity.ActivityType;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ActivityService activityService;

    public GoalService(GoalRepository goalRepository, UserRepository userRepository,
                        CurrentUserProvider currentUserProvider, ActivityService activityService) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.activityService = activityService;
    }

    public List<GoalResponse> getAllGoals() {
        Long userId = currentUserProvider.getCurrentUserId();
        return goalRepository.findAllByUserId(userId).stream()
                .map(GoalResponse::fromEntity)
                .toList();
    }

    public GoalResponse getGoalById(Long id) {
        Goal goal = findGoalOrThrow(id);
        return GoalResponse.fromEntity(goal);
    }

    public GoalResponse createGoal(CreateGoalRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);
        Goal goal = new Goal(request.title(), request.description(), request.targetDate(), GoalStatus.ACTIVE, user);
        Goal savedGoal = goalRepository.save(goal);
        activityService.record(ActivityType.GOAL_CREATED, savedGoal.getId());
        return GoalResponse.fromEntity(savedGoal);
    }


    public GoalResponse updateGoal(Long id, UpdateGoalRequest request) {
        Goal goal = findGoalOrThrow(id);
        boolean wasActive = goal.getStatus() == GoalStatus.ACTIVE;
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setTargetDate(request.targetDate());
        goal.setStatus(request.status());
        Goal savedGoal = goalRepository.save(goal);
        if (wasActive && savedGoal.getStatus() == GoalStatus.COMPLETED) {
            activityService.record(ActivityType.GOAL_COMPLETED, savedGoal.getId());
        }
        return GoalResponse.fromEntity(savedGoal);
    }

    public void deleteGoal(Long id) {
        Goal goal = findGoalOrThrow(id);
        goalRepository.delete(goal);
    }

    private Goal findGoalOrThrow(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new GoalNotFoundException(id));
    }



}