package com.overvu.backend.activity;

import java.time.LocalDate;

public record DailyActivityCount(
        LocalDate date,
        long count
) {
}