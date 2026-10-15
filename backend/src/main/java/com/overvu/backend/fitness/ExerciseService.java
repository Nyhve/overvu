package com.overvu.backend.fitness;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll().stream()
                .map(ExerciseResponse::fromEntity)
                .toList();
    }

    public ExerciseResponse createExercise(CreateExerciseRequest request) {
        if (exerciseRepository.existsByName(request.name())) {
            throw new ExerciseAlreadyExistsException(request.name());
        }
        Exercise exercise = new Exercise(request.name(), request.muscleGroup());
        return ExerciseResponse.fromEntity(exerciseRepository.save(exercise));
    }

    public Exercise findExerciseOrThrow(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
    }
}