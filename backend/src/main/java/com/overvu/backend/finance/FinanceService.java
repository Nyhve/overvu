package com.overvu.backend.finance;

import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinanceService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public FinanceService(TransactionRepository transactionRepository,
                           UserRepository userRepository,
                           CurrentUserProvider currentUserProvider) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<TransactionResponse> getAllTransactions() {
        Long userId = currentUserProvider.getCurrentUserId();
        return transactionRepository.findAllByUserIdOrderByDateDesc(userId).stream()
                .map(TransactionResponse::fromEntity)
                .toList();
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);
        Transaction transaction = new Transaction(
                user,
                request.amount(),
                request.description(),
                request.category(),
                request.type(),
                request.date()
        );
        return TransactionResponse.fromEntity(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        transactionRepository.delete(transaction);
    }

    public MonthlyStatsResponse getMonthlyStats(int year, int month) {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<Transaction> transactions = transactionRepository
                .findAllByUserIdAndDateBetween(userId, from, to);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyStatsResponse(year, month, totalIncome, totalExpenses,
                totalIncome.subtract(totalExpenses));
    }

    public List<MonthlyFinanceSummary> getLastMonthsSummary(int months) {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDate now = LocalDate.now();

        List<MonthlyFinanceSummary> result = new java.util.ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                LocalDate from = month.withDayOfMonth(1);
                LocalDate to = month.withDayOfMonth(month.lengthOfMonth());

                List<Transaction> transactions =
                        transactionRepository.findAllByUserIdAndDateBetween(userId, from, to);

                BigDecimal income = transactions.stream()
                        .filter(t -> t.getType() == TransactionType.INCOME)
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal expenses = transactions.stream()
                        .filter(t -> t.getType() == TransactionType.EXPENSE)
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                String[] monthNames = {
                "Jan", "Feb", "Mär", "Apr", "Mai", "Jun",
                "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"
                };
                String label = monthNames[month.getMonthValue() - 1] + " " + month.getYear();

                result.add(new MonthlyFinanceSummary(
                        month.getYear(), month.getMonthValue(), label, income, expenses
                ));
        }
        return result;
        }
}