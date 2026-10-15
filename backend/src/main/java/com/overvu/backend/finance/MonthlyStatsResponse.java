package com.overvu.backend.finance;

import java.math.BigDecimal;

public record MonthlyStatsResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance
) {
}