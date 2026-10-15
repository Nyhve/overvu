package com.overvu.backend.activity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public List<ActivityResponse> getActivityFeed() {
        return activityService.getActivityFeed();
    }

    @GetMapping("/daily")
    public List<DailyActivityCount> getDailyActivityCounts(
            @RequestParam(defaultValue = "30") int days) {
        return activityService.getDailyActivityCounts(days);
    }
}