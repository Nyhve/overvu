package com.overvu.backend.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.overvu.backend.security.JwtService;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getAllTasks_returnsOkAndList() throws Exception {
        TaskResponse task = new TaskResponse(1L, "Task", "Beschreibung", LocalDate.of(2026, 9, 1),
                false, LocalDateTime.now(), LocalDateTime.now());
        when(taskService.getAllTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task"));
    }

    @Test
    void getTaskById_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        when(taskService.getTaskById(999L)).thenThrow(new TaskNotFoundException(999L));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }

    @Test
    void createTask_returnsCreated_whenRequestIsValid() throws Exception {
        TaskResponse response = new TaskResponse(1L, "Neuer Task", "Beschreibung", LocalDate.of(2026, 9, 1),
                false, LocalDateTime.now(), LocalDateTime.now());
        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(response);

        CreateTaskRequest request = new CreateTaskRequest("Neuer Task", "Beschreibung", LocalDate.of(2026, 9, 1));

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Neuer Task"));
    }

    @Test
    void createTask_returnsBadRequest_whenTitleIsBlank() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("", null, null);

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTask_returnsNoContent_whenTaskExists() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }
}