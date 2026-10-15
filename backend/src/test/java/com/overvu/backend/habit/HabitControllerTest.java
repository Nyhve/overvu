package com.overvu.backend.habit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import com.overvu.backend.security.JwtService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitController.class)
@AutoConfigureMockMvc(addFilters = false)
class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HabitService habitService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getAllHabits_returnsOkAndList() throws Exception {
        HabitResponse habit = new HabitResponse(1L, "Meditieren", null, HabitFrequency.DAILY, LocalDateTime.now());
        when(habitService.getAllHabits()).thenReturn(List.of(habit));

        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Meditieren"));
    }

    @Test
    void createHabit_returnsCreated_whenRequestIsValid() throws Exception {
        HabitResponse response = new HabitResponse(1L, "Meditieren", null, HabitFrequency.DAILY, LocalDateTime.now());
        when(habitService.createHabit(any(CreateHabitRequest.class))).thenReturn(response);

        CreateHabitRequest request = new CreateHabitRequest("Meditieren", null, HabitFrequency.DAILY);

        mockMvc.perform(post("/api/habits")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Meditieren"));
    }

    @Test
    void createHabit_returnsBadRequest_whenNameIsBlank() throws Exception {
        CreateHabitRequest request = new CreateHabitRequest("", null, HabitFrequency.DAILY);

        mockMvc.perform(post("/api/habits")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeToday_returnsCreated() throws Exception {
        HabitEntryResponse entry = new HabitEntryResponse(1L, 1L, LocalDate.now(), LocalDateTime.now());
        when(habitService.completeToday(1L)).thenReturn(entry);

        mockMvc.perform(post("/api/habits/1/entries"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.habitId").value(1));
    }

    @Test
    void completeToday_returnsConflict_whenAlreadyCompletedToday() throws Exception {
        when(habitService.completeToday(1L)).thenThrow(new HabitAlreadyCompletedTodayException());

        mockMvc.perform(post("/api/habits/1/entries"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Habit already completed today"));
    }
}