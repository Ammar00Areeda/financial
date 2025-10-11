package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.Transaction;
import com.financial.entity.User;
import com.financial.repository.TransactionRepository;
import com.financial.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Category testCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(User.Role.USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .build();

        testAccount = Account.builder()
                .id(1L)
                .name("Test Account")
                .type(Account.AccountType.BANK_ACCOUNT)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .user(testUser)
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Food")
                .type(Category.CategoryType.EXPENSE)
                .user(testUser)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .description("Test Transaction")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .account(testAccount)
                .category(testCategory)
                .transactionDate(LocalDateTime.now())
                .user(testUser)
                .isRecurring(false)
                .build();
    }

    @Test
    void getAllTransactions_WithPagination_ShouldReturnPageOfTransactions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(Arrays.asList(testTransaction));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByUser(testUser, pageable)).thenReturn(transactionPage);

            // Act
            Page<Transaction> result = transactionService.getAllTransactions(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testTransaction.getId(), result.getContent().get(0).getId());
            verify(transactionRepository).findByUser(testUser, pageable);
        }
    }

    @Test
    void getAllTransactions_WithoutPagination_ShouldReturnAllTransactions() {
        // Arrange
        Page<Transaction> transactionPage = new PageImpl<>(Arrays.asList(testTransaction));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(transactionPage);

            // Act
            List<Transaction> result = transactionService.getAllTransactions();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testTransaction.getId(), result.get(0).getId());
            verify(transactionRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void getTransactionById_WhenTransactionExists_ShouldReturnTransaction() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTransaction));

            // Act
            Optional<Transaction> result = transactionService.getTransactionById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testTransaction.getId(), result.get().getId());
            verify(transactionRepository).findByIdAndUser(1L, testUser);
        }
    }

    @Test
    void getTransactionById_WhenTransactionDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act
            Optional<Transaction> result = transactionService.getTransactionById(999L);

            // Assert
            assertFalse(result.isPresent());
            verify(transactionRepository).findByIdAndUser(999L, testUser);
        }
    }

    @Test
    void createTransaction_WithValidData_ShouldCreateTransaction() {
        // Arrange
        Transaction newTransaction = Transaction.builder()
                .description("New Transaction")
                .amount(new BigDecimal("50.00"))
                .type(Transaction.TransactionType.INCOME)
                .account(testAccount)
                .transactionDate(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountService.getAccountById(testAccount.getId())).thenReturn(Optional.of(testAccount));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(newTransaction);

            // Act
            Transaction result = transactionService.createTransaction(newTransaction);

            // Assert
            assertNotNull(result);
            assertEquals("New Transaction", result.getDescription());
            assertEquals(testUser, newTransaction.getUser());
            verify(accountService).getAccountById(testAccount.getId());
            verify(transactionRepository).save(newTransaction);
        }
    }

    @Test
    void createTransaction_WithInvalidAccount_ShouldThrowException() {
        // Arrange
        Transaction newTransaction = Transaction.builder()
                .description("New Transaction")
                .amount(new BigDecimal("50.00"))
                .type(Transaction.TransactionType.INCOME)
                .account(testAccount)
                .transactionDate(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountService.getAccountById(testAccount.getId())).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> transactionService.createTransaction(newTransaction)
            );

            assertTrue(exception.getMessage().contains("Account not found"));
            verify(accountService).getAccountById(testAccount.getId());
            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Test
    void updateTransaction_WithValidData_ShouldUpdateTransaction() {
        // Arrange
        Transaction updatedTransaction = Transaction.builder()
                .id(1L)
                .description("Updated Transaction")
                .amount(new BigDecimal("150.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .account(testAccount)
                .transactionDate(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTransaction));
            when(accountService.getAccountById(testAccount.getId())).thenReturn(Optional.of(testAccount));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);

            // Act
            Transaction result = transactionService.updateTransaction(updatedTransaction);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Transaction", result.getDescription());
            assertEquals(testUser, updatedTransaction.getUser());
            verify(transactionRepository).findByIdAndUser(1L, testUser);
            verify(transactionRepository).save(updatedTransaction);
        }
    }

    @Test
    void updateTransaction_WhenTransactionNotFound_ShouldThrowException() {
        // Arrange
        Transaction updatedTransaction = Transaction.builder()
                .id(999L)
                .description("Updated Transaction")
                .amount(new BigDecimal("150.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .account(testAccount)
                .transactionDate(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> transactionService.updateTransaction(updatedTransaction)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(transactionRepository).findByIdAndUser(999L, testUser);
            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Test
    void deleteTransaction_WhenTransactionExists_ShouldDeleteTransaction() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTransaction));
            doNothing().when(transactionRepository).delete(testTransaction);

            // Act
            transactionService.deleteTransaction(1L);

            // Assert
            verify(transactionRepository).findByIdAndUser(1L, testUser);
            verify(transactionRepository).delete(testTransaction);
        }
    }

    @Test
    void deleteTransaction_WhenTransactionNotFound_ShouldThrowException() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> transactionService.deleteTransaction(999L)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(transactionRepository).findByIdAndUser(999L, testUser);
            verify(transactionRepository, never()).delete(any(Transaction.class));
        }
    }

    @Test
    void getTransactionsByType_ShouldReturnTransactionsOfSpecifiedType() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(testTransaction);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByUserAndType(testUser, Transaction.TransactionType.EXPENSE))
                    .thenReturn(transactions);

            // Act
            List<Transaction> result = transactionService.getTransactionsByType(Transaction.TransactionType.EXPENSE);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Transaction.TransactionType.EXPENSE, result.get(0).getType());
            verify(transactionRepository).findByUserAndType(testUser, Transaction.TransactionType.EXPENSE);
        }
    }

    @Test
    void getTransactionsByDateRange_ShouldReturnTransactionsInRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Transaction> transactions = Arrays.asList(testTransaction);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByUserAndTransactionDateBetween(testUser, startDate, endDate))
                    .thenReturn(transactions);

            // Act
            List<Transaction> result = transactionService.getTransactionsByDateRange(startDate, endDate);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(transactionRepository).findByUserAndTransactionDateBetween(testUser, startDate, endDate);
        }
    }

    @Test
    void calculateTotalAmountByType_ShouldReturnCorrectTotal() {
        // Arrange
        BigDecimal expectedAmount = new BigDecimal("500.00");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.calculateTotalAmountByUserAndType(testUser, Transaction.TransactionType.INCOME))
                    .thenReturn(expectedAmount);

            // Act
            BigDecimal result = transactionService.calculateTotalAmountByType(Transaction.TransactionType.INCOME);

            // Assert
            assertNotNull(result);
            assertEquals(expectedAmount, result);
            verify(transactionRepository).calculateTotalAmountByUserAndType(testUser, Transaction.TransactionType.INCOME);
        }
    }

    @Test
    void searchTransactionsByDescription_ShouldReturnMatchingTransactions() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(testTransaction);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findByUserAndDescriptionContainingIgnoreCase(testUser, "Test"))
                    .thenReturn(transactions);

            // Act
            List<Transaction> result = transactionService.searchTransactionsByDescription("Test");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getDescription().contains("Test"));
            verify(transactionRepository).findByUserAndDescriptionContainingIgnoreCase(testUser, "Test");
        }
    }

    @Test
    void getLastTransactions_ShouldReturnLimitedTransactions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> transactionPage = new PageImpl<>(Arrays.asList(testTransaction));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionRepository.findLastTransactionsByUser(testUser, pageable)).thenReturn(transactionPage);

            // Act
            List<Transaction> result = transactionService.getLastTransactions(5);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(transactionRepository).findLastTransactionsByUser(testUser, pageable);
        }
    }

    @Test
    void getTransactionsByAccount_ShouldReturnAccountTransactions() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByAccount(testAccount)).thenReturn(transactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsByAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount.getId(), result.get(0).getAccount().getId());
        verify(transactionRepository).findByAccount(testAccount);
    }

    @Test
    void getTransactionsByCategory_ShouldReturnCategoryTransactions() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCategory(testCategory)).thenReturn(transactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsByCategory(testCategory);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategory.getId(), result.get(0).getCategory().getId());
        verify(transactionRepository).findByCategory(testCategory);
    }

    @Test
    void calculateTotalIncomeByAccount_ShouldReturnCorrectAmount() {
        // Arrange
        BigDecimal expectedIncome = new BigDecimal("1500.00");

        when(transactionRepository.calculateTotalIncomeByAccount(testAccount)).thenReturn(expectedIncome);

        // Act
        BigDecimal result = transactionService.calculateTotalIncomeByAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(expectedIncome, result);
        verify(transactionRepository).calculateTotalIncomeByAccount(testAccount);
    }

    @Test
    void calculateTotalExpenseByAccount_ShouldReturnCorrectAmount() {
        // Arrange
        BigDecimal expectedExpense = new BigDecimal("800.00");

        when(transactionRepository.calculateTotalExpenseByAccount(testAccount)).thenReturn(expectedExpense);

        // Act
        BigDecimal result = transactionService.calculateTotalExpenseByAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(expectedExpense, result);
        verify(transactionRepository).calculateTotalExpenseByAccount(testAccount);
    }
}

