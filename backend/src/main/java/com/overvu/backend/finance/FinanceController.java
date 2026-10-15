package com.overvu.backend.finance;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return financeService.getAllTransactions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        return financeService.createTransaction(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable Long id) {
        financeService.deleteTransaction(id);
    }

    @GetMapping("/stats/{year}/{month}")
    public MonthlyStatsResponse getMonthlyStats(@PathVariable int year, @PathVariable int month) {
        return financeService.getMonthlyStats(year, month);
    }

    @GetMapping("/summary")
    public List<MonthlyFinanceSummary> getLastMonthsSummary(
            @RequestParam(defaultValue = "6") int months) {
        return financeService.getLastMonthsSummary(months);
    }
}