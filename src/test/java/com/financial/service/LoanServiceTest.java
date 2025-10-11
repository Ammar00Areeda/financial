package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.entity.User;
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
 * Unit tests for LoanService.
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    private User testUser;
    private Account testAccount;
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
                .user(testUser)
                .build();

        testLoan = Loan.builder()
                .id(1L)
                .personName("John Doe")
                .phoneNumber("1234567890")
                .email("john@example.com")
                .loanType(Loan.LoanType.LENT)
                .principalAmount(new BigDecimal("5000.00"))
                .interestRate(new BigDecimal("5.00"))
                .totalAmount(new BigDecimal("5250.00"))
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(new BigDecimal("5250.00"))
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusMonths(6))
                .status(Loan.LoanStatus.ACTIVE)
                .account(testAccount)
                .user(testUser)
                .isUrgent(false)
                .reminderEnabled(true)
                .build();
    }

    @Test
    void getAllLoans_WithPagination_ShouldReturnPageOfLoans() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> loanPage = new PageImpl<>(Arrays.asList(testLoan));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByUser(testUser, pageable)).thenReturn(loanPage);

            // Act
            Page<Loan> result = loanService.getAllLoans(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testLoan.getId(), result.getContent().get(0).getId());
            verify(loanRepository).findByUser(testUser, pageable);
        }
    }

    @Test
    void getAllLoans_WithoutPagination_ShouldReturnAllLoans() {
        // Arrange
        Page<Loan> loanPage = new PageImpl<>(Arrays.asList(testLoan));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(loanPage);

            // Act
            List<Loan> result = loanService.getAllLoans();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testLoan.getId(), result.get(0).getId());
            verify(loanRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void getLoanById_WhenLoanExists_ShouldReturnLoan() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testLoan));

            // Act
            Optional<Loan> result = loanService.getLoanById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testLoan.getId(), result.get().getId());
            verify(loanRepository).findByIdAndUser(1L, testUser);
        }
    }

    @Test
    void getLoanById_WhenLoanDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act
            Optional<Loan> result = loanService.getLoanById(999L);

            // Assert
            assertFalse(result.isPresent());
            verify(loanRepository).findByIdAndUser(999L, testUser);
        }
    }

    @Test
    void getLoansByType_ShouldReturnLoansOfSpecifiedType() {
        // Arrange
        List<Loan> loans = Arrays.asList(testLoan);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByUserAndLoanType(testUser, Loan.LoanType.LENT)).thenReturn(loans);

            // Act
            List<Loan> result = loanService.getLoansByType(Loan.LoanType.LENT);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Loan.LoanType.LENT, result.get(0).getLoanType());
            verify(loanRepository).findByUserAndLoanType(testUser, Loan.LoanType.LENT);
        }
    }

    @Test
    void getLoansByStatus_ShouldReturnLoansOfSpecifiedStatus() {
        // Arrange
        List<Loan> loans = Arrays.asList(testLoan);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByUserAndStatus(testUser, Loan.LoanStatus.ACTIVE)).thenReturn(loans);

            // Act
            List<Loan> result = loanService.getLoansByStatus(Loan.LoanStatus.ACTIVE);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Loan.LoanStatus.ACTIVE, result.get(0).getStatus());
            verify(loanRepository).findByUserAndStatus(testUser, Loan.LoanStatus.ACTIVE);
        }
    }

    @Test
    void getLoansByTypeAndStatus_ShouldReturnMatchingLoans() {
        // Arrange
        List<Loan> loans = Arrays.asList(testLoan);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByUserAndLoanType(testUser, Loan.LoanType.LENT)).thenReturn(loans);

            // Act
            List<Loan> result = loanService.getLoansByTypeAndStatus(Loan.LoanType.LENT, Loan.LoanStatus.ACTIVE);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Loan.LoanType.LENT, result.get(0).getLoanType());
            assertEquals(Loan.LoanStatus.ACTIVE, result.get(0).getStatus());
            verify(loanRepository).findByUserAndLoanType(testUser, Loan.LoanType.LENT);
        }
    }

    @Test
    void getLoansByAccount_ShouldReturnAccountLoans() {
        // Arrange
        List<Loan> loans = Arrays.asList(testLoan);
        when(loanRepository.findByAccount(testAccount)).thenReturn(loans);

        // Act
        List<Loan> result = loanService.getLoansByAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount.getId(), result.get(0).getAccount().getId());
        verify(loanRepository).findByAccount(testAccount);
    }

    @Test
    void getLoansByAccountAndType_ShouldReturnMatchingLoans() {
        // Arrange
        List<Loan> loans = Arrays.asList(testLoan);
        when(loanRepository.findByAccountAndLoanType(testAccount, Loan.LoanType.LENT)).thenReturn(loans);

        // Act
        List<Loan> result = loanService.getLoansByAccountAndType(testAccount, Loan.LoanType.LENT);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount.getId(), result.get(0).getAccount().getId());
        assertEquals(Loan.LoanType.LENT, result.get(0).getLoanType());
        verify(loanRepository).findByAccountAndLoanType(testAccount, Loan.LoanType.LENT);
    }

    @Test
    void searchLoansByPersonName_ShouldReturnMatchingLoans() {
        // Arrange
        Page<Loan> loanPage = new PageImpl<>(Arrays.asList(testLoan));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(loanPage);

            // Act
            List<Loan> result = loanService.searchLoansByPersonName("John");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getPersonName().toLowerCase().contains("john"));
            verify(loanRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void getOverdueLoans_ShouldReturnOverdueLoans() {
        // Arrange
        Loan overdueLoan = Loan.builder()
                .id(2L)
                .personName("Jane Doe")
                .loanType(Loan.LoanType.LENT)
                .principalAmount(new BigDecimal("3000.00"))
                .totalAmount(new BigDecimal("3000.00"))
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(new BigDecimal("3000.00"))
                .loanDate(LocalDateTime.now().minusMonths(3))
                .dueDate(LocalDateTime.now().minusDays(10))
                .status(Loan.LoanStatus.ACTIVE)
                .user(testUser)
                .build();
        List<Loan> overdueLoans = Arrays.asList(overdueLoan);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findOverdueLoansByUser(eq(testUser), any(LocalDateTime.class))).thenReturn(overdueLoans);

            // Act
            List<Loan> result = loanService.getOverdueLoans();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(loanRepository).findOverdueLoansByUser(eq(testUser), any(LocalDateTime.class));
        }
    }

    @Test
    void getUrgentLoans_ShouldReturnUrgentLoans() {
        // Arrange
        testLoan.setIsUrgent(true);
        Page<Loan> loanPage = new PageImpl<>(Arrays.asList(testLoan));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByUser(testUser, Pageable.unpaged())).thenReturn(loanPage);

            // Act
            List<Loan> result = loanService.getUrgentLoans();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsUrgent());
            verify(loanRepository).findByUser(testUser, Pageable.unpaged());
        }
    }

    @Test
    void createLoan_WithValidData_ShouldCreateLoan() {
        // Arrange
        Loan newLoan = Loan.builder()
                .personName("New Person")
                .loanType(Loan.LoanType.BORROWED)
                .principalAmount(new BigDecimal("2000.00"))
                .interestRate(new BigDecimal("3.00"))
                .loanDate(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.save(any(Loan.class))).thenReturn(newLoan);

            // Act
            Loan result = loanService.createLoan(newLoan);

            // Assert
            assertNotNull(result);
            assertEquals("New Person", result.getPersonName());
            assertEquals(testUser, newLoan.getUser());
            assertNotNull(newLoan.getTotalAmount());
            assertNotNull(newLoan.getRemainingAmount());
            verify(loanRepository).save(newLoan);
        }
    }

    @Test
    void createLoan_WithoutInterest_ShouldSetTotalAmountToPrincipal() {
        // Arrange
        Loan newLoan = Loan.builder()
                .personName("New Person")
                .loanType(Loan.LoanType.LENT)
                .principalAmount(new BigDecimal("1000.00"))
                .loanDate(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.save(any(Loan.class))).thenReturn(newLoan);

            // Act
            Loan result = loanService.createLoan(newLoan);

            // Assert
            assertNotNull(result);
            assertEquals(newLoan.getPrincipalAmount(), newLoan.getTotalAmount());
            assertEquals(newLoan.getTotalAmount(), newLoan.getRemainingAmount());
            verify(loanRepository).save(newLoan);
        }
    }

    @Test
    void updateLoan_WithValidData_ShouldUpdateLoan() {
        // Arrange
        Loan updatedLoan = Loan.builder()
                .id(1L)
                .personName("Updated Person")
                .loanType(Loan.LoanType.LENT)
                .principalAmount(new BigDecimal("6000.00"))
                .totalAmount(new BigDecimal("6000.00"))
                .loanDate(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testLoan));
            when(loanRepository.save(any(Loan.class))).thenReturn(updatedLoan);

            // Act
            Loan result = loanService.updateLoan(updatedLoan);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Person", result.getPersonName());
            assertEquals(testUser, updatedLoan.getUser());
            verify(loanRepository).findByIdAndUser(1L, testUser);
            verify(loanRepository).save(updatedLoan);
        }
    }

    @Test
    void updateLoan_WhenLoanNotFound_ShouldThrowException() {
        // Arrange
        Loan updatedLoan = Loan.builder()
                .id(999L)
                .personName("Updated Person")
                .loanType(Loan.LoanType.LENT)
                .principalAmount(new BigDecimal("6000.00"))
                .loanDate(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> loanService.updateLoan(updatedLoan)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(loanRepository).findByIdAndUser(999L, testUser);
            verify(loanRepository, never()).save(any(Loan.class));
        }
    }

    @Test
    void deleteLoan_WhenLoanExists_ShouldDeleteLoan() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testLoan));
            doNothing().when(loanRepository).delete(testLoan);

            // Act
            loanService.deleteLoan(1L);

            // Assert
            verify(loanRepository).findByIdAndUser(1L, testUser);
            verify(loanRepository).delete(testLoan);
        }
    }

    @Test
    void deleteLoan_WhenLoanNotFound_ShouldThrowException() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> loanService.deleteLoan(999L)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(loanRepository).findByIdAndUser(999L, testUser);
            verify(loanRepository, never()).delete(any(Loan.class));
        }
    }

    @Test
    void recordPayment_ShouldUpdateLoanCorrectly() {
        // Arrange
        BigDecimal paymentAmount = new BigDecimal("1000.00");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testLoan));
            when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Loan result = loanService.recordPayment(1L, paymentAmount);

            // Assert
            assertNotNull(result);
            assertEquals(paymentAmount, result.getPaidAmount());
            assertEquals(new BigDecimal("4250.00"), result.getRemainingAmount());
            assertEquals(Loan.LoanStatus.PARTIALLY_PAID, result.getStatus());
            assertNotNull(result.getLastPaymentDate());
            verify(loanRepository).findByIdAndUser(1L, testUser);
            verify(loanRepository).save(testLoan);
        }
    }

    @Test
    void recordPayment_WhenFullyPaid_ShouldSetStatusToPaidOff() {
        // Arrange
        BigDecimal paymentAmount = new BigDecimal("5250.00");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testLoan));
            when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Loan result = loanService.recordPayment(1L, paymentAmount);

            // Assert
            assertNotNull(result);
            assertEquals(paymentAmount, result.getPaidAmount());
            assertEquals(0, result.getRemainingAmount().compareTo(BigDecimal.ZERO));
            assertEquals(Loan.LoanStatus.PAID_OFF, result.getStatus());
            verify(loanRepository).save(testLoan);
        }
    }

    @Test
    void markAsUrgent_ShouldSetIsUrgentToTrue() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testLoan));
            when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Loan result = loanService.markAsUrgent(1L);

            // Assert
            assertNotNull(result);
            assertTrue(result.getIsUrgent());
            verify(loanRepository).save(testLoan);
        }
    }

    @Test
    void markAsNotUrgent_ShouldSetIsUrgentToFalse() {
        // Arrange
        testLoan.setIsUrgent(true);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testLoan));
            when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Loan result = loanService.markAsNotUrgent(1L);

            // Assert
            assertNotNull(result);
            assertFalse(result.getIsUrgent());
            verify(loanRepository).save(testLoan);
        }
    }

    @Test
    void getLoanSummaryReport_ShouldReturnCorrectSummary() {
        // Arrange
        BigDecimal totalLent = new BigDecimal("10000.00");
        BigDecimal totalBorrowed = new BigDecimal("5000.00");
        List<Loan> lentLoans = Arrays.asList(testLoan);
        List<Loan> borrowedLoans = Arrays.asList();
        List<Loan> overdueLoans = Arrays.asList();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.calculateTotalAmountLentByUser(testUser)).thenReturn(totalLent);
            when(loanRepository.calculateTotalAmountBorrowedByUser(testUser)).thenReturn(totalBorrowed);
            when(loanRepository.findByUserAndLoanType(testUser, Loan.LoanType.LENT)).thenReturn(lentLoans);
            when(loanRepository.findByUserAndLoanType(testUser, Loan.LoanType.BORROWED)).thenReturn(borrowedLoans);
            when(loanRepository.findOverdueLoansByUser(eq(testUser), any(LocalDateTime.class))).thenReturn(overdueLoans);

            // Act
            LoanService.LoanSummaryReport result = loanService.getLoanSummaryReport();

            // Assert
            assertNotNull(result);
            assertEquals(totalLent, result.getTotalAmountLent());
            assertEquals(totalBorrowed, result.getTotalAmountBorrowed());
            assertEquals(new BigDecimal("5000.00"), result.getNetLoanPosition());
            assertEquals(1, result.getActiveLentLoansCount());
            assertEquals(0, result.getActiveBorrowedLoansCount());
            assertEquals(0, result.getOverdueLoansCount());
        }
    }

    @Test
    void getTotalAmountLent_ShouldReturnCorrectAmount() {
        // Arrange
        BigDecimal expectedAmount = new BigDecimal("15000.00");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.calculateTotalAmountLentByUser(testUser)).thenReturn(expectedAmount);

            // Act
            BigDecimal result = loanService.getTotalAmountLent();

            // Assert
            assertNotNull(result);
            assertEquals(expectedAmount, result);
            verify(loanRepository).calculateTotalAmountLentByUser(testUser);
        }
    }

    @Test
    void getTotalAmountBorrowed_ShouldReturnCorrectAmount() {
        // Arrange
        BigDecimal expectedAmount = new BigDecimal("8000.00");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.calculateTotalAmountBorrowedByUser(testUser)).thenReturn(expectedAmount);

            // Act
            BigDecimal result = loanService.getTotalAmountBorrowed();

            // Assert
            assertNotNull(result);
            assertEquals(expectedAmount, result);
            verify(loanRepository).calculateTotalAmountBorrowedByUser(testUser);
        }
    }

    @Test
    void getNetLoanPosition_ShouldReturnCorrectPosition() {
        // Arrange
        BigDecimal totalLent = new BigDecimal("15000.00");
        BigDecimal totalBorrowed = new BigDecimal("8000.00");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.calculateTotalAmountLentByUser(testUser)).thenReturn(totalLent);
            when(loanRepository.calculateTotalAmountBorrowedByUser(testUser)).thenReturn(totalBorrowed);

            // Act
            BigDecimal result = loanService.getNetLoanPosition();

            // Assert
            assertNotNull(result);
            assertEquals(new BigDecimal("7000.00"), result);
            verify(loanRepository).calculateTotalAmountLentByUser(testUser);
            verify(loanRepository).calculateTotalAmountBorrowedByUser(testUser);
        }
    }

    @Test
    void getTotalAmountLentByStatus_ShouldReturnCorrectAmount() {
        // Arrange
        List<Loan> loans = Arrays.asList(testLoan);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(loanRepository.findByUserAndLoanType(testUser, Loan.LoanType.LENT)).thenReturn(loans);

            // Act
            BigDecimal result = loanService.getTotalAmountLentByStatus(Loan.LoanStatus.ACTIVE);

            // Assert
            assertNotNull(result);
            assertEquals(testLoan.getPrincipalAmount(), result);
            verify(loanRepository).findByUserAndLoanType(testUser, Loan.LoanType.LENT);
        }
    }
}

