package com.financial.service;

import com.financial.dto.NetWorthResponseDto;
import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.entity.User;
import com.financial.repository.AccountRepository;
import com.financial.repository.LoanRepository;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NetWorthService.
 */
@ExtendWith(MockitoExtension.class)
class NetWorthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private NetWorthService netWorthService;

    private User testUser;
    private List<Account> testAccounts;
    private List<Loan> testLoans;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        // Create test accounts
        Account savingsAccount = Account.builder()
                .id(1L)
                .name("Savings Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("JD")
                .includeInBalance(true)
                .status(Account.AccountStatus.ACTIVE)
                .user(testUser)
                .build();

        Account checkingAccount = Account.builder()
                .id(2L)
                .name("Checking Account")
                .type(Account.AccountType.BANK_ACCOUNT)
                .balance(new BigDecimal("3000.00"))
                .currency("JD")
                .includeInBalance(true)
                .status(Account.AccountStatus.ACTIVE)
                .user(testUser)
                .build();

        testAccounts = List.of(savingsAccount, checkingAccount);

        // Create test loans
        Loan lentLoan = Loan.builder()
                .id(1L)
                .personName("John Doe")
                .loanType(Loan.LoanType.LENT)
                .principalAmount(new BigDecimal("2000.00"))
                .totalAmount(new BigDecimal("2000.00"))
                .paidAmount(new BigDecimal("500.00"))
                .remainingAmount(new BigDecimal("1500.00"))
                .loanDate(LocalDateTime.now().minusMonths(1))
                .status(Loan.LoanStatus.ACTIVE)
                .user(testUser)
                .build();

        Loan borrowedLoan = Loan.builder()
                .id(2L)
                .personName("Jane Smith")
                .loanType(Loan.LoanType.BORROWED)
                .principalAmount(new BigDecimal("1000.00"))
                .totalAmount(new BigDecimal("1000.00"))
                .paidAmount(new BigDecimal("200.00"))
                .remainingAmount(new BigDecimal("800.00"))
                .loanDate(LocalDateTime.now().minusWeeks(2))
                .status(Loan.LoanStatus.ACTIVE)
                .user(testUser)
                .build();

        testLoans = List.of(lentLoan, borrowedLoan);
    }

    @Test
    void calculateNetWorth_ShouldReturnCorrectNetWorth() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);

            when(accountRepository.findByUserAndIncludeInBalanceAndStatus(
                    eq(testUser), eq(true), eq(Account.AccountStatus.ACTIVE)))
                    .thenReturn(testAccounts);

            when(loanRepository.findByUser(eq(testUser.getId()), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(testLoans));

            // When
            NetWorthResponseDto result = netWorthService.calculateNetWorth();

            // Then
            assertNotNull(result);
            assertEquals(new BigDecimal("8000.00"), result.getTotalAccountBalance()); // 5000 + 3000
            assertEquals(new BigDecimal("2000.00"), result.getTotalAmountLent());
            assertEquals(new BigDecimal("1000.00"), result.getTotalAmountBorrowed());
            assertEquals(new BigDecimal("1000.00"), result.getNetLoanPosition()); // 2000 - 1000
            assertEquals(new BigDecimal("9000.00"), result.getTotalNetWorth()); // 8000 + 1000
            assertEquals(2, result.getActiveAccountsCount());
            assertEquals(2, result.getActiveLoansCount());
            assertEquals(0, result.getOverdueLoansCount());
            assertEquals("JD", result.getCurrency());
            assertNotNull(result.getCalculatedAt());
            assertNotNull(result.getAccountBalancesByType());
            assertNotNull(result.getLoanSummaryByType());
        }
    }

    @Test
    void getTotalNetWorth_ShouldReturnCorrectTotal() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);

            when(accountRepository.findByUserAndIncludeInBalanceAndStatus(
                    eq(testUser), eq(true), eq(Account.AccountStatus.ACTIVE)))
                    .thenReturn(testAccounts);

            when(loanRepository.findByUser(eq(testUser.getId()), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(testLoans));

            // When
            BigDecimal result = netWorthService.getTotalNetWorth();

            // Then
            assertEquals(new BigDecimal("9000.00"), result);
        }
    }

    @Test
    void getAccountBalancesByType_ShouldReturnCorrectBreakdown() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);

            when(accountRepository.findByUserAndIncludeInBalanceAndStatus(
                    eq(testUser), eq(true), eq(Account.AccountStatus.ACTIVE)))
                    .thenReturn(testAccounts);

            when(loanRepository.findByUser(eq(testUser.getId()), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(testLoans));

            // When
            List<NetWorthResponseDto.AccountTypeBalance> result = netWorthService.getAccountBalancesByType();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            // Check savings account
            NetWorthResponseDto.AccountTypeBalance savingsBalance = result.stream()
                    .filter(b -> "SAVINGS".equals(b.getAccountType()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(savingsBalance);
            assertEquals("Savings", savingsBalance.getAccountTypeDisplayName());
            assertEquals(new BigDecimal("5000.00"), savingsBalance.getTotalBalance());
            assertEquals(1, savingsBalance.getAccountCount());

            // Check bank account
            NetWorthResponseDto.AccountTypeBalance checkingBalance = result.stream()
                    .filter(b -> "BANK_ACCOUNT".equals(b.getAccountType()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(checkingBalance);
            assertEquals("Bank Account", checkingBalance.getAccountTypeDisplayName());
            assertEquals(new BigDecimal("3000.00"), checkingBalance.getTotalBalance());
            assertEquals(1, checkingBalance.getAccountCount());
        }
    }

    @Test
    void getLoanSummaryByType_ShouldReturnCorrectBreakdown() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);

            when(accountRepository.findByUserAndIncludeInBalanceAndStatus(
                    eq(testUser), eq(true), eq(Account.AccountStatus.ACTIVE)))
                    .thenReturn(testAccounts);

            when(loanRepository.findByUser(eq(testUser.getId()), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(testLoans));

            // When
            List<NetWorthResponseDto.LoanTypeSummary> result = netWorthService.getLoanSummaryByType();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            // Check lent loans
            NetWorthResponseDto.LoanTypeSummary lentSummary = result.stream()
                    .filter(s -> "LENT".equals(s.getLoanType()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(lentSummary);
            assertEquals("Lent Money", lentSummary.getLoanTypeDisplayName());
            assertEquals(new BigDecimal("2000.00"), lentSummary.getTotalAmount());
            assertEquals(new BigDecimal("500.00"), lentSummary.getTotalPaid());
            assertEquals(new BigDecimal("1500.00"), lentSummary.getRemainingAmount());
            assertEquals(1, lentSummary.getLoanCount());
            assertEquals(1, lentSummary.getActiveLoanCount());
            assertEquals(0, lentSummary.getOverdueLoanCount());

            // Check borrowed loans
            NetWorthResponseDto.LoanTypeSummary borrowedSummary = result.stream()
                    .filter(s -> "BORROWED".equals(s.getLoanType()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(borrowedSummary);
            assertEquals("Borrowed Money", borrowedSummary.getLoanTypeDisplayName());
            assertEquals(new BigDecimal("1000.00"), borrowedSummary.getTotalAmount());
            assertEquals(new BigDecimal("200.00"), borrowedSummary.getTotalPaid());
            assertEquals(new BigDecimal("800.00"), borrowedSummary.getRemainingAmount());
            assertEquals(1, borrowedSummary.getLoanCount());
            assertEquals(1, borrowedSummary.getActiveLoanCount());
            assertEquals(0, borrowedSummary.getOverdueLoanCount());
        }
    }
}
