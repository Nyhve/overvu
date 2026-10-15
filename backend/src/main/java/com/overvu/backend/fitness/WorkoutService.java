package com.overvu.backend.fitness;

import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.activity.ActivityType;
import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final ExerciseService exerciseService;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ActivityService activityService;

    public WorkoutService(WorkoutRepository workoutRepository,
                           WorkoutExerciseRepository workoutExerciseRepository,
                           WorkoutSetRepository workoutSetRepository,
                           ExerciseService exerciseService,
                           UserRepository userRepository,
                           CurrentUserProvider currentUserProvider,
                           ActivityService activityService) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.exerciseService = exerciseService;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.activityService = activityService;
    }

    public List<WorkoutResponse> getAllWorkouts() {
        Long userId = currentUserProvider.getCurrentUserId();
        return workoutRepository.findAllByUserIdWithExercises(userId).stream()
                .map(WorkoutResponse::fromEntity)
                .toList();
    }

    public WorkoutResponse getWorkoutById(Long id) {
        return WorkoutResponse.fromEntity(findWorkoutOrThrow(id));
    }

    public WorkoutResponse createWorkout(CreateWorkoutRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);
        Workout workout = new Workout(user, request.name(), request.date(), request.notes());
        return WorkoutResponse.fromEntity(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutResponse completeWorkout(Long id) {
        Workout workout = findWorkoutOrThrow(id);
        if (workout.isCompleted()) {
            throw new WorkoutAlreadyCompletedException();
        }
        workout.setCompleted(true);
        Workout saved = workoutRepository.save(workout);
        activityService.record(ActivityType.WORKOUT_COMPLETED, id);
        return WorkoutResponse.fromEntity(saved);
    }

    public void deleteWorkout(Long id) {
        Workout workout = findWorkoutOrThrow(id);
        workoutRepository.delete(workout);
    }

    @Transactional
    public WorkoutResponse addExercise(Long workoutId, AddExerciseToWorkoutRequest request) {
        Workout workout = findWorkoutOrThrow(workoutId);
        if (workout.isCompleted()) {
            throw new WorkoutAlreadyCompletedException();
        }
        Exercise exercise = exerciseService.findExerciseOrThrow(request.exerciseId());
        int orderIndex = workout.getExercises().size();
        WorkoutExercise workoutExercise = new WorkoutExercise(workout, exercise, orderIndex);
        workout.getExercises().add(workoutExercise);
        return WorkoutResponse.fromEntity(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutResponse removeExercise(Long workoutId, Long workoutExerciseId) {
        Workout workout = findWorkoutOrThrow(workoutId);
        if (workout.isCompleted()) {
            throw new WorkoutAlreadyCompletedException();
        }
        WorkoutExercise toRemove = workoutExerciseRepository
                .findByIdAndWorkoutId(workoutExerciseId, workoutId)
                .orElseThrow(() -> new WorkoutNotFoundException(workoutExerciseId));
        workout.getExercises().remove(toRemove);
        return WorkoutResponse.fromEntity(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutResponse addSet(Long workoutId, Long workoutExerciseId, AddSetRequest request) {
        Workout workout = findWorkoutOrThrow(workoutId);
        if (workout.isCompleted()) {
            throw new WorkoutAlreadyCompletedException();
        }
        WorkoutExercise workoutExercise = workoutExerciseRepository
                .findByIdAndWorkoutId(workoutExerciseId, workoutId)
                .orElseThrow(() -> new WorkoutNotFoundException(workoutExerciseId));
        int setNumber = workoutExercise.getSets().size() + 1;
        WorkoutSet set = new WorkoutSet(workoutExercise, setNumber, request.weightKg(), request.reps());
        workoutExercise.getSets().add(set);
        workoutRepository.save(workout);
        return WorkoutResponse.fromEntity(workoutRepository.findByIdAndUserId(workoutId,
                currentUserProvider.getCurrentUserId()).orElseThrow());
    }

    @Transactional
    public WorkoutResponse removeSet(Long workoutId, Long workoutExerciseId, Long setId) {
        Workout workout = findWorkoutOrThrow(workoutId);
        if (workout.isCompleted()) {
            throw new WorkoutAlreadyCompletedException();
        }
        WorkoutExercise workoutExercise = workoutExerciseRepository
                .findByIdAndWorkoutId(workoutExerciseId, workoutId)
                .orElseThrow(() -> new WorkoutNotFoundException(workoutExerciseId));
        WorkoutSet toRemove = workoutSetRepository
                .findByIdAndWorkoutExerciseId(setId, workoutExerciseId)
                .orElseThrow(() -> new WorkoutNotFoundException(setId));
        workoutExercise.getSets().remove(toRemove);
        return WorkoutResponse.fromEntity(workoutRepository.save(workout));
    }

    private Workout findWorkoutOrThrow(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return workoutRepository.findByIdAndUserIdWithExercises(id, userId)
                .orElseThrow(() -> new WorkoutNotFoundException(id));
    }
}