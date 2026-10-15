package com.overvu.backend.habit;

import com.overvu.backend.activity.ActivityService;
import com.overvu.backend.activity.ActivityType;
import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.time.temporal.ChronoUnit;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitEntryRepository habitEntryRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ActivityService activityService;

    public HabitService(HabitRepository habitRepository,
                         HabitEntryRepository habitEntryRepository,
                         UserRepository userRepository,
                         CurrentUserProvider currentUserProvider,
                         ActivityService activityService) {
        this.habitRepository = habitRepository;
        this.habitEntryRepository = habitEntryRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.activityService = activityService;
    }

    public List<HabitResponse> getAllHabits() {
        Long userId = currentUserProvider.getCurrentUserId();
        return habitRepository.findAllByUserId(userId).stream()
                .map(HabitResponse::fromEntity)
                .toList();
    }

    public List<HabitEntryResponse> getTodaysEntries() {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDate today = LocalDate.now();
        return habitEntryRepository.findAllByHabitUserIdAndDate(userId, today)
                .stream()
                .map(HabitEntryResponse::fromEntity)
                .toList();
    }

    public HabitResponse createHabit(CreateHabitRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);
        Habit habit = new Habit(user, request.name(), request.description(), request.frequency());
        Habit savedHabit = habitRepository.save(habit);
        return HabitResponse.fromEntity(savedHabit);
    }

    public void deleteHabit(Long id) {
        Habit habit = findHabitOrThrow(id);
        habitRepository.delete(habit);
    }

    public HabitEntryResponse completeToday(Long habitId) {
        Habit habit = findHabitOrThrow(habitId);
        LocalDate today = LocalDate.now();

        if (habitEntryRepository.existsByHabitIdAndDate(habitId, today)) {
            throw new HabitAlreadyCompletedTodayException();
        }

        HabitEntry entry = new HabitEntry(habit, today);
        HabitEntry savedEntry = habitEntryRepository.save(entry);
        activityService.record(ActivityType.HABIT_COMPLETED, habitId);
        return HabitEntryResponse.fromEntity(savedEntry);
    }

    public List<HabitEntryResponse> getEntries(Long habitId) {
        findHabitOrThrow(habitId);
        return habitEntryRepository.findAllByHabitId(habitId).stream()
                .map(HabitEntryResponse::fromEntity)
                .toList();
    }

    public void deleteEntry(Long habitId, Long entryId) {
        findHabitOrThrow(habitId);
        HabitEntry entry = habitEntryRepository.findById(entryId)
                .orElseThrow(() -> new HabitNotFoundException(entryId));
        habitEntryRepository.delete(entry);
    }

    private Habit findHabitOrThrow(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new HabitNotFoundException(id));
    }

    public List<HabitConsistencyResponse> getConsistency(int days) {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDate from = LocalDate.now().minusDays(days - 1);
        List<Habit> habits = habitRepository.findAllByUserId(userId);

        return habits.stream().map(habit -> {
            List<HabitEntry> entries = habitEntryRepository.findAllByHabitId(habit.getId());

            long completedDays = entries.stream()
                    .filter(e -> !e.getDate().isBefore(from))
                    .count();

            int totalDays = habit.getFrequency() == HabitFrequency.DAILY
                    ? days
                    : days / 7;

            double percent = totalDays == 0 ? 0
                    : Math.round((completedDays * 100.0 / totalDays) * 10.0) / 10.0;

            return new HabitConsistencyResponse(
                    habit.getId(),
                    habit.getName(),
                    habit.getFrequency(),
                    totalDays,
                    (int) completedDays,
                    Math.min(percent, 100.0)
            );
        }).toList();
    }
}