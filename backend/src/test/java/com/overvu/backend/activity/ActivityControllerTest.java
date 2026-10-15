package com.overvu.backend.activity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.overvu.backend.security.JwtService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getActivityFeed_returnsOkAndList() throws Exception {
        ActivityResponse response = new ActivityResponse(
                1L, ActivityType.GOAL_CREATED, 5L, LocalDateTime.now()
        );
        when(activityService.getActivityFeed()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("GOAL_CREATED"))
                .andExpect(jsonPath("$[0].referenceId").value(5));
    }

    @Test
    void getActivityFeed_returnsEmptyList_whenNoActivities() throws Exception {
        when(activityService.getActivityFeed()).thenReturn(List.of());

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}