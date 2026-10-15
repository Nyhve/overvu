package com.overvu.backend.finance;

import java.math.BigDecimal;

public record MonthlyFinanceSummary(
        int year,
        int month,
        String monthLabel,
        BigDecimal income,
        BigDecimal expenses
) {
}