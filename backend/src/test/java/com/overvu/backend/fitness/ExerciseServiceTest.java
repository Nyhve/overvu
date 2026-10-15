package com.overvu.backend.fitness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    void createExercise_savesAndReturnsExercise() {
        CreateExerciseRequest request = new CreateExerciseRequest("Bankdrücken", MuscleGroup.CHEST);
        when(exerciseRepository.existsByName("Bankdrücken")).thenReturn(false);
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(i -> i.getArgument(0));

        ExerciseResponse response = exerciseService.createExercise(request);

        assertThat(response.name()).isEqualTo("Bankdrücken");
        assertThat(response.muscleGroup()).isEqualTo(MuscleGroup.CHEST);
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void createExercise_throwsException_whenNameAlreadyExists() {
        CreateExerciseRequest request = new CreateExerciseRequest("Bankdrücken", MuscleGroup.CHEST);
        when(exerciseRepository.existsByName("Bankdrücken")).thenReturn(true);

        assertThatThrownBy(() -> exerciseService.createExercise(request))
                .isInstanceOf(ExerciseAlreadyExistsException.class);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void getAllExercises_returnsEmptyList_whenNoExercisesExist() {
        when(exerciseRepository.findAll()).thenReturn(List.of());

        List<ExerciseResponse> result = exerciseService.getAllExercises();

        assertThat(result).isEmpty();
    }

    @Test
    void findExerciseOrThrow_throwsException_whenExerciseDoesNotExist() {
        when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.findExerciseOrThrow(999L))
                .isInstanceOf(ExerciseNotFoundException.class)
                .hasMessageContaining("999");
    }
}