package com.overvu.backend.fitness;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public List<WorkoutResponse> getAllWorkouts() {
        return workoutService.getAllWorkouts();
    }

    @GetMapping("/{id}")
    public WorkoutResponse getWorkoutById(@PathVariable Long id) {
        return workoutService.getWorkoutById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse createWorkout(@Valid @RequestBody CreateWorkoutRequest request) {
        return workoutService.createWorkout(request);
    }

    @PostMapping("/{id}/complete")
    public WorkoutResponse completeWorkout(@PathVariable Long id) {
        return workoutService.completeWorkout(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkout(@PathVariable Long id) {
        workoutService.deleteWorkout(id);
    }

    @PostMapping("/{id}/exercises")
    public WorkoutResponse addExercise(@PathVariable Long id,
                                        @Valid @RequestBody AddExerciseToWorkoutRequest request) {
        return workoutService.addExercise(id, request);
    }

    @DeleteMapping("/{id}/exercises/{workoutExerciseId}")
    public WorkoutResponse removeExercise(@PathVariable Long id,
                                           @PathVariable Long workoutExerciseId) {
        return workoutService.removeExercise(id, workoutExerciseId);
    }

    @PostMapping("/{id}/exercises/{workoutExerciseId}/sets")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse addSet(@PathVariable Long id,
                                   @PathVariable Long workoutExerciseId,
                                   @Valid @RequestBody AddSetRequest request) {
        return workoutService.addSet(id, workoutExerciseId, request);
    }

    @DeleteMapping("/{id}/exercises/{workoutExerciseId}/sets/{setId}")
    public WorkoutResponse removeSet(@PathVariable Long id,
                                      @PathVariable Long workoutExerciseId,
                                      @PathVariable Long setId) {
        return workoutService.removeSet(id, workoutExerciseId, setId);
    }
}