package com.financial.controller;

import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.entity.Transaction;
import com.financial.entity.User;
import com.financial.security.JwtUtil;
import com.financial.security.SecurityUtils;
import com.financial.service.AccountService;
import com.financial.service.LoanService;
import com.financial.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DashboardController.
 */
@WebMvcTest(controllers = DashboardController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class
    })
@Import(com.financial.config.TestSecurityConfig.class)
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private LoanService loanService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private com.financial.service.UserService userService;

    private User testUser;
    private Account testAccount;
    private Transaction testTransaction;
    private Loan testLoan;

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
                .includeInBalance(true)
                .user(testUser)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .description("Test Transaction")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .account(testAccount)
                .transactionDate(LocalDateTime.now())
                .user(testUser)
                .build();

        testLoan = Loan.builder()
                .id(1L)
                .description("Test Loan")
                .personName("John Doe")
                .principalAmount(new BigDecimal("5000.00"))
                .totalAmount(new BigDecimal("5000.00"))
                .loanType(Loan.LoanType.LENT)
                .status(Loan.LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .account(testAccount)
                .user(testUser)
                .build();

        // Set up security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
        );
    }

    @Test
    void getDashboard_ShouldReturnDashboardData() throws Exception {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount);
        List<Transaction> transactions = Arrays.asList(testTransaction);
        List<Loan> loans = Arrays.asList(testLoan);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountService.getAllAccounts()).thenReturn(accounts);
            when(transactionService.getAllTransactions(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(transactions));
            when(loanService.getLoansByStatus(Loan.LoanStatus.ACTIVE)).thenReturn(loans);
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(transactions);

            // Act & Assert
            mockMvc.perform(get("/api/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.netWorth").exists())
                    .andExpect(jsonPath("$.netWorth.current").value(1000.00))
                    .andExpect(jsonPath("$.netWorth.currency").value("JOD"))
                    .andExpect(jsonPath("$.accounts").isArray())
                    .andExpect(jsonPath("$.accounts[0].id").value("1"))
                    .andExpect(jsonPath("$.accounts[0].name").value("Test Account"))
                    .andExpect(jsonPath("$.accounts[0].balance").value(1000.00))
                    .andExpect(jsonPath("$.recentTransactions").isArray())
                    .andExpect(jsonPath("$.recentTransactions[0].description").value("Test Transaction"))
                    .andExpect(jsonPath("$.activeLoans").isArray())
                    .andExpect(jsonPath("$.activeLoans[0].counterparty").value("John Doe"))
                    .andExpect(jsonPath("$.monthlySpending").exists());

            verify(accountService).getAllAccounts();
            verify(transactionService).getAllTransactions(any(Pageable.class));
            verify(loanService).getLoansByStatus(Loan.LoanStatus.ACTIVE);
        }
    }

    @Test
    void getDashboard_WithNoAccounts_ShouldReturnZeroNetWorth() throws Exception {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountService.getAllAccounts()).thenReturn(Arrays.asList());
            when(transactionService.getAllTransactions(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Arrays.asList()));
            when(loanService.getLoansByStatus(Loan.LoanStatus.ACTIVE)).thenReturn(Arrays.asList());
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList());

            // Act & Assert
            mockMvc.perform(get("/api/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.netWorth.current").value(0))
                    .andExpect(jsonPath("$.accounts").isEmpty())
                    .andExpect(jsonPath("$.recentTransactions").isEmpty())
                    .andExpect(jsonPath("$.activeLoans").isEmpty());
        }
    }

    @Test
    void getDashboard_WithMultipleAccounts_ShouldCalculateTotalNetWorth() throws Exception {
        // Arrange
        Account account2 = Account.builder()
                .id(2L)
                .name("Savings Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();

        List<Account> accounts = Arrays.asList(testAccount, account2);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountService.getAllAccounts()).thenReturn(accounts);
            when(transactionService.getAllTransactions(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Arrays.asList()));
            when(loanService.getLoansByStatus(Loan.LoanStatus.ACTIVE)).thenReturn(Arrays.asList());
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList());

            // Act & Assert
            mockMvc.perform(get("/api/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.netWorth.current").value(3000.00))
                    .andExpect(jsonPath("$.accounts.length()").value(2));
        }
    }

    @Test
    void getDashboard_WithAccountNotIncludedInBalance_ShouldExcludeFromNetWorth() throws Exception {
        // Arrange
        testAccount.setIncludeInBalance(false);
        List<Account> accounts = Arrays.asList(testAccount);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountService.getAllAccounts()).thenReturn(accounts);
            when(transactionService.getAllTransactions(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Arrays.asList()));
            when(loanService.getLoansByStatus(Loan.LoanStatus.ACTIVE)).thenReturn(Arrays.asList());
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList());

            // Act & Assert
            mockMvc.perform(get("/api/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.netWorth.current").value(0));
        }
    }

    @Test
    void getDashboard_WithExpenseTransactions_ShouldIncludeInMonthlySpending() throws Exception {
        // Arrange
        Transaction expenseTransaction = Transaction.builder()
                .id(2L)
                .description("Expense Transaction")
                .amount(new BigDecimal("200.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .account(testAccount)
                .transactionDate(LocalDateTime.now())
                .user(testUser)
                .build();

        List<Transaction> transactions = Arrays.asList(expenseTransaction);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountService.getAllAccounts()).thenReturn(Arrays.asList(testAccount));
            when(transactionService.getAllTransactions(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(transactions));
            when(loanService.getLoansByStatus(Loan.LoanStatus.ACTIVE)).thenReturn(Arrays.asList());
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(transactions);

            // Act & Assert
            mockMvc.perform(get("/api/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.monthlySpending").exists())
                    .andExpect(jsonPath("$.recentTransactions[0].type").value("expense"));
        }
    }
}

