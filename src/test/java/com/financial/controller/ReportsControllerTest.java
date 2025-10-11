package com.financial.controller;

import com.financial.entity.Transaction;
import com.financial.entity.User;
import com.financial.security.JwtUtil;
import com.financial.security.SecurityUtils;
import com.financial.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
 * Unit tests for ReportsController.
 */
@WebMvcTest(controllers = ReportsController.class, 
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class
    })
@Import(com.financial.config.TestSecurityConfig.class)
@ActiveProfiles("test")
class ReportsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private com.financial.service.UserService userService;

    private User testUser;
    private Transaction incomeTransaction;
    private Transaction expenseTransaction;

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

        incomeTransaction = Transaction.builder()
                .id(1L)
                .description("Salary")
                .amount(new BigDecimal("5000.00"))
                .type(Transaction.TransactionType.INCOME)
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 0))
                .user(testUser)
                .build();

        expenseTransaction = Transaction.builder()
                .id(2L)
                .description("Groceries")
                .amount(new BigDecimal("300.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .transactionDate(LocalDateTime.of(2024, 1, 20, 14, 30))
                .user(testUser)
                .build();

        // Set up security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
        );
    }

    @Test
    void getMonthlyReports_WithValidDateRange_ShouldReturnReports() throws Exception {
        // Arrange
        List<Transaction> transactions = Arrays.asList(incomeTransaction, expenseTransaction);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(transactions);

            // Act & Assert
            mockMvc.perform(get("/api/reports/monthly")
                            .param("start", "2024-01")
                            .param("end", "2024-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reports").isArray())
                    .andExpect(jsonPath("$.reports[0].month").value("2024-01"))
                    .andExpect(jsonPath("$.reports[0].income").value(5000.00))
                    .andExpect(jsonPath("$.reports[0].expenses").value(300.00))
                    .andExpect(jsonPath("$.reports[0].savings").value(4700.00))
                    .andExpect(jsonPath("$.totals.income").value(5000.00))
                    .andExpect(jsonPath("$.totals.expenses").value(300.00))
                    .andExpect(jsonPath("$.totals.savings").value(4700.00));

            verify(transactionService, atLeastOnce()).getTransactionsByDateRange(
                    any(LocalDateTime.class), any(LocalDateTime.class)
            );
        }
    }

    @Test
    void getMonthlyReports_WithMultipleMonths_ShouldReturnMultipleReports() throws Exception {
        // Arrange
        Transaction januaryTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("1000.00"))
                .type(Transaction.TransactionType.INCOME)
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        Transaction februaryTransaction = Transaction.builder()
                .id(2L)
                .amount(new BigDecimal("500.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .transactionDate(LocalDateTime.of(2024, 2, 15, 10, 0))
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            
            // Mock different responses for different date ranges
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> {
                        LocalDateTime start = invocation.getArgument(0);
                        if (start.getMonthValue() == 1) {
                            return Arrays.asList(januaryTransaction);
                        } else {
                            return Arrays.asList(februaryTransaction);
                        }
                    });

            // Act & Assert
            mockMvc.perform(get("/api/reports/monthly")
                            .param("start", "2024-01")
                            .param("end", "2024-02"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reports").isArray())
                    .andExpect(jsonPath("$.reports.length()").value(2))
                    .andExpect(jsonPath("$.reports[0].month").value("2024-01"))
                    .andExpect(jsonPath("$.reports[1].month").value("2024-02"));
        }
    }

    @Test
    void getMonthlyReports_WithNoTransactions_ShouldReturnZeroAmounts() throws Exception {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList());

            // Act & Assert
            mockMvc.perform(get("/api/reports/monthly")
                            .param("start", "2024-06")
                            .param("end", "2024-06"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reports[0].income").value(0))
                    .andExpect(jsonPath("$.reports[0].expenses").value(0))
                    .andExpect(jsonPath("$.reports[0].savings").value(0))
                    .andExpect(jsonPath("$.totals.income").value(0))
                    .andExpect(jsonPath("$.totals.expenses").value(0))
                    .andExpect(jsonPath("$.totals.savings").value(0));
        }
    }

    @Test
    void getMonthlyReports_WithInvalidDateFormat_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reports/monthly")
                        .param("start", "invalid-date")
                        .param("end", "2024-01"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getMonthlyReports_WithMissingParameters_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reports/monthly"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getMonthlyReports_WithOnlyExpenses_ShouldReturnNegativeSavings() throws Exception {
        // Arrange
        List<Transaction> transactions = Arrays.asList(expenseTransaction);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(transactionService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(transactions);

            // Act & Assert
            mockMvc.perform(get("/api/reports/monthly")
                            .param("start", "2024-01")
                            .param("end", "2024-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reports[0].income").value(0))
                    .andExpect(jsonPath("$.reports[0].expenses").value(300.00))
                    .andExpect(jsonPath("$.reports[0].savings").value(-300.00));
        }
    }
}

