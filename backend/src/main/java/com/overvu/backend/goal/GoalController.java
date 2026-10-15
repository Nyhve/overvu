package com.overvu.backend.goal;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public List<GoalResponse> getAllGoals() {
        return goalService.getAllGoals();
    }

    @GetMapping("/{id}")
    public GoalResponse getGoalById(@PathVariable Long id) {
        return goalService.getGoalById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse createGoal(@Valid @RequestBody CreateGoalRequest request) {
        return goalService.createGoal(request);
    }

    @PutMapping("/{id}")
    public GoalResponse updateGoal(@PathVariable Long id, @Valid @RequestBody UpdateGoalRequest request) {
        return goalService.updateGoal(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
    }
}