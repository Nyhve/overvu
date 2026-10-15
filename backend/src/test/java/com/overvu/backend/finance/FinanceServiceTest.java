package com.overvu.backend.finance;

import com.overvu.backend.security.CurrentUserProvider;
import com.overvu.backend.user.User;
import com.overvu.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private FinanceService financeService;

    private final Long userId = 1L;
    private final User user = new User("test@example.com", "hashedValue");

    @Test
    void createTransaction_savesAndReturnsTransaction() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("3000.00"),
                "Gehalt",
                TransactionCategory.SALARY,
                TransactionType.INCOME,
                LocalDate.now()
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        TransactionResponse response = financeService.createTransaction(request);

        assertThat(response.amount()).isEqualByComparingTo("3000.00");
        assertThat(response.type()).isEqualTo(TransactionType.INCOME);
        assertThat(response.category()).isEqualTo(TransactionCategory.SALARY);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deleteTransaction_throwsException_whenTransactionDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(transactionRepository.findByIdAndUserId(999L, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> financeService.deleteTransaction(999L))
                .isInstanceOf(TransactionNotFoundException.class);

        verify(transactionRepository, never()).delete(any());
    }

    @Test
    void getMonthlyStats_calculatesCorrectly() {
        Transaction income = new Transaction(user, new BigDecimal("3000.00"),
                "Gehalt", TransactionCategory.SALARY, TransactionType.INCOME,
                LocalDate.of(2026, 10, 1));
        Transaction expense = new Transaction(user, new BigDecimal("150.00"),
                "Lebensmittel", TransactionCategory.FOOD, TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 5));

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(transactionRepository.findAllByUserIdAndDateBetween(
                eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(income, expense));

        MonthlyStatsResponse stats = financeService.getMonthlyStats(2026, 10);

        assertThat(stats.totalIncome()).isEqualByComparingTo("3000.00");
        assertThat(stats.totalExpenses()).isEqualByComparingTo("150.00");
        assertThat(stats.balance()).isEqualByComparingTo("2850.00");
    }

    @Test
    void getMonthlyStats_returnsZeros_whenNoTransactions() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(transactionRepository.findAllByUserIdAndDateBetween(
                eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        MonthlyStatsResponse stats = financeService.getMonthlyStats(2026, 10);

        assertThat(stats.totalIncome()).isEqualByComparingTo("0.00");
        assertThat(stats.totalExpenses()).isEqualByComparingTo("0.00");
        assertThat(stats.balance()).isEqualByComparingTo("0.00");
    }

    @Test
    void getAllTransactions_returnsEmptyList_whenNoTransactionsExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(transactionRepository.findAllByUserIdOrderByDateDesc(userId))
                .thenReturn(List.of());

        List<TransactionResponse> result = financeService.getAllTransactions();

        assertThat(result).isEmpty();
    }
}