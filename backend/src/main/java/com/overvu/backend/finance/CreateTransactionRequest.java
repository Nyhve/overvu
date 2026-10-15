package com.overvu.backend.finance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotNull(message = "Amount must not be null")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Description must not be blank")
        String description,

        @NotNull(message = "Category must not be null")
        TransactionCategory category,

        @NotNull(message = "Type must not be null")
        TransactionType type,

        @NotNull(message = "Date must not be null")
        LocalDate date
) {
}