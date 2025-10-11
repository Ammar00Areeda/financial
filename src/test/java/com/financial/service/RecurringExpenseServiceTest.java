package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.RecurringExpense;
import com.financial.entity.Transaction;
import com.financial.entity.User;
import com.financial.repository.RecurringExpenseRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecurringExpenseService.
 */
@ExtendWith(MockitoExtension.class)
class RecurringExpenseServiceTest {

    @Mock
    private RecurringExpenseRepository recurringExpenseRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private RecurringExpenseService recurringExpenseService;

    private User testUser;
    private Account testAccount;
    private Category testCategory;
    private RecurringExpense testRecurringExpense;

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
                .name("Subscriptions")
                .type(Category.CategoryType.EXPENSE)
                .user(testUser)
                .build();

        testRecurringExpense = RecurringExpense.builder()
                .id(1L)
                .name("Netflix Subscription")
                .description("Monthly streaming service")
                .amount(new BigDecimal("15.99"))
                .frequency(RecurringExpense.Frequency.MONTHLY)
                .account(testAccount)
                .category(testCategory)
                .startDate(LocalDate.now().minusMonths(1))
                .nextDueDate(LocalDate.now().plusDays(5))
                .status(RecurringExpense.Status.ACTIVE)
                .isAutoPay(false)
                .reminderDaysBefore(3)
                .provider("Netflix")
                .referenceNumber("NET-12345")
                .user(testUser)
                .build();
    }

    @Test
    void getAllRecurringExpenses_WithPagination_ShouldReturnPageOfExpenses() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<RecurringExpense> expensePage = new PageImpl<>(Arrays.asList(testRecurringExpense));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByUser(testUser, pageable)).thenReturn(expensePage);

            // Act
            Page<RecurringExpense> result = recurringExpenseService.getAllRecurringExpenses(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testRecurringExpense.getId(), result.getContent().get(0).getId());
            verify(recurringExpenseRepository).findByUser(testUser, pageable);
        }
    }

    @Test
    void getAllRecurringExpenses_WithoutPagination_ShouldReturnAllExpenses() {
        // Arrange
        Page<RecurringExpense> expensePage = new PageImpl<>(Arrays.asList(testRecurringExpense));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(expensePage);

            // Act
            List<RecurringExpense> result = recurringExpenseService.getAllRecurringExpenses();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testRecurringExpense.getId(), result.get(0).getId());
            verify(recurringExpenseRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void getRecurringExpenseById_WhenExpenseExists_ShouldReturnExpense() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testRecurringExpense));

            // Act
            Optional<RecurringExpense> result = recurringExpenseService.getRecurringExpenseById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testRecurringExpense.getId(), result.get().getId());
            verify(recurringExpenseRepository).findByIdAndUser(1L, testUser);
        }
    }

    @Test
    void getRecurringExpenseById_WhenExpenseDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act
            Optional<RecurringExpense> result = recurringExpenseService.getRecurringExpenseById(999L);

            // Assert
            assertFalse(result.isPresent());
            verify(recurringExpenseRepository).findByIdAndUser(999L, testUser);
        }
    }

    @Test
    void getRecurringExpensesByAccount_ShouldReturnAccountExpenses() {
        // Arrange
        List<RecurringExpense> expenses = Arrays.asList(testRecurringExpense);
        when(recurringExpenseRepository.findByAccount(testAccount)).thenReturn(expenses);

        // Act
        List<RecurringExpense> result = recurringExpenseService.getRecurringExpensesByAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount.getId(), result.get(0).getAccount().getId());
        verify(recurringExpenseRepository).findByAccount(testAccount);
    }

    @Test
    void getRecurringExpensesByStatus_ShouldReturnExpensesOfSpecifiedStatus() {
        // Arrange
        List<RecurringExpense> expenses = Arrays.asList(testRecurringExpense);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByUserAndStatus(testUser, RecurringExpense.Status.ACTIVE)).thenReturn(expenses);

            // Act
            List<RecurringExpense> result = recurringExpenseService.getRecurringExpensesByStatus(RecurringExpense.Status.ACTIVE);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(RecurringExpense.Status.ACTIVE, result.get(0).getStatus());
            verify(recurringExpenseRepository).findByUserAndStatus(testUser, RecurringExpense.Status.ACTIVE);
        }
    }

    @Test
    void getRecurringExpensesByFrequency_ShouldReturnExpensesOfSpecifiedFrequency() {
        // Arrange
        Page<RecurringExpense> expensePage = new PageImpl<>(Arrays.asList(testRecurringExpense));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(expensePage);

            // Act
            List<RecurringExpense> result = recurringExpenseService.getRecurringExpensesByFrequency(RecurringExpense.Frequency.MONTHLY);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(RecurringExpense.Frequency.MONTHLY, result.get(0).getFrequency());
            verify(recurringExpenseRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void getRecurringExpensesByAccountAndStatus_ShouldReturnMatchingExpenses() {
        // Arrange
        List<RecurringExpense> expenses = Arrays.asList(testRecurringExpense);
        when(recurringExpenseRepository.findByAccountAndStatus(testAccount, RecurringExpense.Status.ACTIVE)).thenReturn(expenses);

        // Act
        List<RecurringExpense> result = recurringExpenseService.getRecurringExpensesByAccountAndStatus(testAccount, RecurringExpense.Status.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount.getId(), result.get(0).getAccount().getId());
        assertEquals(RecurringExpense.Status.ACTIVE, result.get(0).getStatus());
        verify(recurringExpenseRepository).findByAccountAndStatus(testAccount, RecurringExpense.Status.ACTIVE);
    }

    @Test
    void searchRecurringExpensesByName_ShouldReturnMatchingExpenses() {
        // Arrange
        Page<RecurringExpense> expensePage = new PageImpl<>(Arrays.asList(testRecurringExpense));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(expensePage);

            // Act
            List<RecurringExpense> result = recurringExpenseService.searchRecurringExpensesByName("Netflix");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getName().toLowerCase().contains("netflix"));
            verify(recurringExpenseRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void searchRecurringExpensesByProvider_ShouldReturnMatchingExpenses() {
        // Arrange
        Page<RecurringExpense> expensePage = new PageImpl<>(Arrays.asList(testRecurringExpense));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(expensePage);

            // Act
            List<RecurringExpense> result = recurringExpenseService.searchRecurringExpensesByProvider("Netflix");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getProvider().toLowerCase().contains("netflix"));
            verify(recurringExpenseRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void getRecurringExpensesDueToday_ShouldReturnDueExpenses() {
        // Arrange
        testRecurringExpense.setNextDueDate(LocalDate.now());
        List<RecurringExpense> expenses = Arrays.asList(testRecurringExpense);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findDueTodayByUser(testUser, LocalDate.now())).thenReturn(expenses);

            // Act
            List<RecurringExpense> result = recurringExpenseService.getRecurringExpensesDueToday();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(recurringExpenseRepository).findDueTodayByUser(testUser, LocalDate.now());
        }
    }

    @Test
    void getOverdueRecurringExpenses_ShouldReturnOverdueExpenses() {
        // Arrange
        testRecurringExpense.setNextDueDate(LocalDate.now().minusDays(5));
        List<RecurringExpense> expenses = Arrays.asList(testRecurringExpense);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findOverdueByUser(testUser, LocalDate.now())).thenReturn(expenses);

            // Act
            List<RecurringExpense> result = recurringExpenseService.getOverdueRecurringExpenses();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(recurringExpenseRepository).findOverdueByUser(testUser, LocalDate.now());
        }
    }

    @Test
    void getRecurringExpensesWithAutoPay_ShouldReturnAutoPayExpenses() {
        // Arrange
        testRecurringExpense.setIsAutoPay(true);
        Page<RecurringExpense> expensePage = new PageImpl<>(Arrays.asList(testRecurringExpense));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(expensePage);

            // Act
            List<RecurringExpense> result = recurringExpenseService.getRecurringExpensesWithAutoPay();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsAutoPay());
            verify(recurringExpenseRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void getRecurringExpensesByAmountRange_ShouldReturnExpensesInRange() {
        // Arrange
        BigDecimal minAmount = new BigDecimal("10.00");
        BigDecimal maxAmount = new BigDecimal("20.00");
        List<RecurringExpense> expenses = Arrays.asList(testRecurringExpense);
        when(recurringExpenseRepository.findByAmountBetween(minAmount, maxAmount)).thenReturn(expenses);

        // Act
        List<RecurringExpense> result = recurringExpenseService.getRecurringExpensesByAmountRange(minAmount, maxAmount);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recurringExpenseRepository).findByAmountBetween(minAmount, maxAmount);
    }

    @Test
    void createRecurringExpense_WithValidData_ShouldCreateExpense() {
        // Arrange
        RecurringExpense newExpense = RecurringExpense.builder()
                .name("Spotify Premium")
                .amount(new BigDecimal("9.99"))
                .frequency(RecurringExpense.Frequency.MONTHLY)
                .account(testAccount)
                .startDate(LocalDate.now())
                .status(RecurringExpense.Status.ACTIVE)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.save(any(RecurringExpense.class))).thenReturn(newExpense);

            // Act
            RecurringExpense result = recurringExpenseService.createRecurringExpense(newExpense);

            // Assert
            assertNotNull(result);
            assertEquals("Spotify Premium", result.getName());
            assertEquals(testUser, newExpense.getUser());
            verify(recurringExpenseRepository).save(newExpense);
        }
    }

    @Test
    void updateRecurringExpense_WithValidData_ShouldUpdateExpense() {
        // Arrange
        RecurringExpense updatedExpense = RecurringExpense.builder()
                .id(1L)
                .name("Netflix Premium")
                .amount(new BigDecimal("19.99"))
                .frequency(RecurringExpense.Frequency.MONTHLY)
                .account(testAccount)
                .startDate(LocalDate.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testRecurringExpense));
            when(recurringExpenseRepository.save(any(RecurringExpense.class))).thenReturn(updatedExpense);

            // Act
            RecurringExpense result = recurringExpenseService.updateRecurringExpense(updatedExpense);

            // Assert
            assertNotNull(result);
            assertEquals("Netflix Premium", result.getName());
            assertEquals(testUser, updatedExpense.getUser());
            verify(recurringExpenseRepository).findByIdAndUser(1L, testUser);
            verify(recurringExpenseRepository).save(updatedExpense);
        }
    }

    @Test
    void updateRecurringExpense_WhenExpenseNotFound_ShouldThrowException() {
        // Arrange
        RecurringExpense updatedExpense = RecurringExpense.builder()
                .id(999L)
                .name("Netflix Premium")
                .amount(new BigDecimal("19.99"))
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> recurringExpenseService.updateRecurringExpense(updatedExpense)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(recurringExpenseRepository).findByIdAndUser(999L, testUser);
            verify(recurringExpenseRepository, never()).save(any(RecurringExpense.class));
        }
    }

    @Test
    void deleteRecurringExpense_WhenExpenseExists_ShouldDeleteExpense() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testRecurringExpense));
            doNothing().when(recurringExpenseRepository).delete(testRecurringExpense);

            // Act
            recurringExpenseService.deleteRecurringExpense(1L);

            // Assert
            verify(recurringExpenseRepository).findByIdAndUser(1L, testUser);
            verify(recurringExpenseRepository).delete(testRecurringExpense);
        }
    }

    @Test
    void deleteRecurringExpense_WhenExpenseNotFound_ShouldThrowException() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> recurringExpenseService.deleteRecurringExpense(999L)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(recurringExpenseRepository).findByIdAndUser(999L, testUser);
            verify(recurringExpenseRepository, never()).delete(any(RecurringExpense.class));
        }
    }

    @Test
    void markAsPaid_ShouldUpdateExpenseAndCreateTransaction() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testRecurringExpense));
            when(recurringExpenseRepository.save(any(RecurringExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(new Transaction());
            when(accountService.subtractFromAccountBalance(eq(1L), any(BigDecimal.class))).thenReturn(testAccount);

            // Act
            RecurringExpense result = recurringExpenseService.markAsPaid(1L);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getLastPaidDate());
            verify(recurringExpenseRepository).save(testRecurringExpense);
            verify(transactionService).createTransaction(any(Transaction.class));
            verify(accountService).subtractFromAccountBalance(eq(1L), eq(testRecurringExpense.getAmount()));
        }
    }

    @Test
    void pauseRecurringExpense_ShouldSetStatusToPaused() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testRecurringExpense));
            when(recurringExpenseRepository.save(any(RecurringExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            RecurringExpense result = recurringExpenseService.pauseRecurringExpense(1L);

            // Assert
            assertNotNull(result);
            assertEquals(RecurringExpense.Status.PAUSED, result.getStatus());
            verify(recurringExpenseRepository).save(testRecurringExpense);
        }
    }

    @Test
    void resumeRecurringExpense_ShouldSetStatusToActive() {
        // Arrange
        testRecurringExpense.setStatus(RecurringExpense.Status.PAUSED);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testRecurringExpense));
            when(recurringExpenseRepository.save(any(RecurringExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            RecurringExpense result = recurringExpenseService.resumeRecurringExpense(1L);

            // Assert
            assertNotNull(result);
            assertEquals(RecurringExpense.Status.ACTIVE, result.getStatus());
            verify(recurringExpenseRepository).save(testRecurringExpense);
        }
    }

    @Test
    void cancelRecurringExpense_ShouldSetStatusToCancelled() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(recurringExpenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testRecurringExpense));
            when(recurringExpenseRepository.save(any(RecurringExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            RecurringExpense result = recurringExpenseService.cancelRecurringExpense(1L);

            // Assert
            assertNotNull(result);
            assertEquals(RecurringExpense.Status.CANCELLED, result.getStatus());
            verify(recurringExpenseRepository).save(testRecurringExpense);
        }
    }

    @Test
    void calculateTotalMonthlyRecurringExpenses_ShouldReturnCorrectTotal() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("99.99");
        when(recurringExpenseRepository.calculateTotalMonthlyRecurringExpenses(testAccount)).thenReturn(expectedTotal);

        // Act
        BigDecimal result = recurringExpenseService.calculateTotalMonthlyRecurringExpenses(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(recurringExpenseRepository).calculateTotalMonthlyRecurringExpenses(testAccount);
    }

    @Test
    void calculateTotalRecurringExpensesByFrequency_ShouldReturnCorrectTotal() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("45.97");
        when(recurringExpenseRepository.calculateTotalRecurringExpensesByFrequency(testAccount, RecurringExpense.Frequency.MONTHLY))
                .thenReturn(expectedTotal);

        // Act
        BigDecimal result = recurringExpenseService.calculateTotalRecurringExpensesByFrequency(testAccount, RecurringExpense.Frequency.MONTHLY);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(recurringExpenseRepository).calculateTotalRecurringExpensesByFrequency(testAccount, RecurringExpense.Frequency.MONTHLY);
    }

    @Test
    void calculateTotalRecurringExpenses_ShouldReturnCorrectTotal() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("150.00");
        when(recurringExpenseRepository.calculateTotalRecurringExpenses(testAccount)).thenReturn(expectedTotal);

        // Act
        BigDecimal result = recurringExpenseService.calculateTotalRecurringExpenses(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(recurringExpenseRepository).calculateTotalRecurringExpenses(testAccount);
    }

    @Test
    void getRecurringExpensesByReferenceNumber_ShouldReturnMatchingExpenses() {
        // Arrange
        List<RecurringExpense> expenses = Arrays.asList(testRecurringExpense);
        when(recurringExpenseRepository.findByReferenceNumber("NET-12345")).thenReturn(expenses);

        // Act
        List<RecurringExpense> result = recurringExpenseService.getRecurringExpensesByReferenceNumber("NET-12345");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NET-12345", result.get(0).getReferenceNumber());
        verify(recurringExpenseRepository).findByReferenceNumber("NET-12345");
    }
}

