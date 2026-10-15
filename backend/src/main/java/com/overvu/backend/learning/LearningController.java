package com.overvu.backend.learning;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping
    public List<LearningProjectResponse> getAllProjects() {
        return learningService.getAllProjects();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LearningProjectResponse createProject(@Valid @RequestBody CreateLearningProjectRequest request) {
        return learningService.createProject(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long id) {
        learningService.deleteProject(id);
    }

    @GetMapping("/{id}/sessions")
    public List<LearningSessionResponse> getSessions(@PathVariable Long id) {
        return learningService.getSessions(id);
    }

    @PostMapping("/{id}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public LearningSessionResponse addSession(@PathVariable Long id,
                                               @Valid @RequestBody CreateLearningSessionRequest request) {
        return learningService.addSession(id, request);
    }

    @DeleteMapping("/{id}/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable Long id, @PathVariable Long sessionId) {
        learningService.deleteSession(id, sessionId);
    }
}