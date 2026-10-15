package com.overvu.backend.habit;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    public List<HabitResponse> getAllHabits() {
        return habitService.getAllHabits();
    }

    @GetMapping("/entries/today")
    public List<HabitEntryResponse> getTodaysEntries() {
        return habitService.getTodaysEntries();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse createHabit(@Valid @RequestBody CreateHabitRequest request) {
        return habitService.createHabit(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
    }

    @PostMapping("/{id}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public HabitEntryResponse completeToday(@PathVariable Long id) {
        return habitService.completeToday(id);
    }

    @GetMapping("/{id}/entries")
    public List<HabitEntryResponse> getEntries(@PathVariable Long id) {
        return habitService.getEntries(id);
    }

    @DeleteMapping("/{id}/entries/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable Long id, @PathVariable Long entryId) {
        habitService.deleteEntry(id, entryId);
    }

    @GetMapping("/consistency")
    public List<HabitConsistencyResponse> getConsistency(
            @RequestParam(defaultValue = "28") int days) {
        return habitService.getConsistency(days);
    }
}