package com.overvu.backend.goal;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.overvu.backend.security.JwtService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.overvu.backend.security.JwtService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GoalService goalService;
    
    @MockitoBean
    private JwtService jwtService;

    @Test
    void getAllGoals_returnsOkAndList() throws Exception {
        GoalResponse goal = new GoalResponse(1L, "Ziel", "Beschreibung", LocalDate.of(2026, 12, 31),
                GoalStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        when(goalService.getAllGoals()).thenReturn(List.of(goal));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Ziel"));
    }

    @Test
    void getGoalById_returnsNotFound_whenGoalDoesNotExist() throws Exception {
        when(goalService.getGoalById(999L)).thenThrow(new GoalNotFoundException(999L));

        mockMvc.perform(get("/api/goals/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal not found with id: 999"));
    }

    @Test
    void createGoal_returnsCreated_whenRequestIsValid() throws Exception {
        GoalResponse response = new GoalResponse(1L, "Neues Ziel", "Beschreibung", LocalDate.of(2026, 12, 31),
                GoalStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        when(goalService.createGoal(any(CreateGoalRequest.class))).thenReturn(response);

        CreateGoalRequest request = new CreateGoalRequest("Neues Ziel", "Beschreibung", LocalDate.of(2026, 12, 31));

        mockMvc.perform(post("/api/goals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Neues Ziel"));
    }

    @Test
    void createGoal_returnsBadRequest_whenTitleIsBlank() throws Exception {
        CreateGoalRequest request = new CreateGoalRequest("", null, null);

        mockMvc.perform(post("/api/goals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteGoal_returnsNoContent_whenGoalExists() throws Exception {
        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isNoContent());
    }
}