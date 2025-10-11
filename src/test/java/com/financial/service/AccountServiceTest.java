package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.User;
import com.financial.repository.AccountRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

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
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllAccounts_WithPagination_ShouldReturnPageOfAccounts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Arrays.asList(testAccount));
        
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByUser(testUser, pageable)).thenReturn(accountPage);

            // Act
            Page<Account> result = accountService.getAllAccounts(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testAccount.getId(), result.getContent().get(0).getId());
            verify(accountRepository).findByUser(testUser, pageable);
        }
    }

    @Test
    void getAllAccounts_WithoutPagination_ShouldReturnActiveAccounts() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount);
        
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByUserAndStatus(testUser, Account.AccountStatus.ACTIVE))
                    .thenReturn(accounts);

            // Act
            List<Account> result = accountService.getAllAccounts();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAccount.getId(), result.get(0).getId());
            verify(accountRepository).findByUserAndStatus(testUser, Account.AccountStatus.ACTIVE);
        }
    }

    @Test
    void getAccountById_WhenAccountExists_ShouldReturnAccount() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAccount));

            // Act
            Optional<Account> result = accountService.getAccountById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testAccount.getId(), result.get().getId());
            verify(accountRepository).findByIdAndUser(1L, testUser);
        }
    }

    @Test
    void getAccountById_WhenAccountDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act
            Optional<Account> result = accountService.getAccountById(999L);

            // Assert
            assertFalse(result.isPresent());
            verify(accountRepository).findByIdAndUser(999L, testUser);
        }
    }

    @Test
    void createAccount_WithValidData_ShouldCreateAccount() {
        // Arrange
        Account newAccount = Account.builder()
                .name("New Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("500.00"))
                .currency("USD")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.existsByNameAndUser("New Account", testUser)).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenReturn(newAccount);

            // Act
            Account result = accountService.createAccount(newAccount);

            // Assert
            assertNotNull(result);
            assertEquals("New Account", result.getName());
            assertEquals(testUser, newAccount.getUser());
            verify(accountRepository).existsByNameAndUser("New Account", testUser);
            verify(accountRepository).save(newAccount);
        }
    }

    @Test
    void createAccount_WithDuplicateName_ShouldThrowException() {
        // Arrange
        Account newAccount = Account.builder()
                .name("Test Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("500.00"))
                .currency("USD")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.existsByNameAndUser("Test Account", testUser)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.createAccount(newAccount)
            );
            
            assertTrue(exception.getMessage().contains("already exists"));
            verify(accountRepository).existsByNameAndUser("Test Account", testUser);
            verify(accountRepository, never()).save(any(Account.class));
        }
    }

    @Test
    void updateAccount_WithValidData_ShouldUpdateAccount() {
        // Arrange
        Account updatedAccount = Account.builder()
                .id(1L)
                .name("Updated Account")
                .type(Account.AccountType.BANK_ACCOUNT)
                .balance(new BigDecimal("2000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAccount));
            when(accountRepository.existsByNameAndUserAndIdNot("Updated Account", testUser, 1L)).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

            // Act
            Account result = accountService.updateAccount(updatedAccount);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Account", result.getName());
            assertEquals(testUser, updatedAccount.getUser());
            verify(accountRepository).findByIdAndUser(1L, testUser);
            verify(accountRepository).save(updatedAccount);
        }
    }

    @Test
    void updateAccount_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        Account updatedAccount = Account.builder()
                .id(999L)
                .name("Updated Account")
                .type(Account.AccountType.BANK_ACCOUNT)
                .balance(new BigDecimal("2000.00"))
                .currency("USD")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.updateAccount(updatedAccount)
            );
            
            assertTrue(exception.getMessage().contains("not found"));
            verify(accountRepository).findByIdAndUser(999L, testUser);
            verify(accountRepository, never()).save(any(Account.class));
        }
    }

    @Test
    void deleteAccount_WhenAccountExists_ShouldDeleteAccount() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAccount));
            doNothing().when(accountRepository).delete(testAccount);

            // Act
            accountService.deleteAccount(1L);

            // Assert
            verify(accountRepository).findByIdAndUser(1L, testUser);
            verify(accountRepository).delete(testAccount);
        }
    }

    @Test
    void deleteAccount_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.deleteAccount(999L)
            );
            
            assertTrue(exception.getMessage().contains("not found"));
            verify(accountRepository).findByIdAndUser(999L, testUser);
            verify(accountRepository, never()).delete(any(Account.class));
        }
    }

    @Test
    void calculateTotalBalance_ShouldReturnCorrectTotal() {
        // Arrange
        BigDecimal expectedBalance = new BigDecimal("5000.00");
        
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.calculateTotalBalanceByUser(testUser, true, Account.AccountStatus.ACTIVE))
                    .thenReturn(expectedBalance);

            // Act
            BigDecimal result = accountService.calculateTotalBalance();

            // Assert
            assertNotNull(result);
            assertEquals(expectedBalance, result);
            verify(accountRepository).calculateTotalBalanceByUser(testUser, true, Account.AccountStatus.ACTIVE);
        }
    }

    @Test
    void getAccountsByType_ShouldReturnAccountsOfSpecifiedType() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount);
        
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByUserAndType(testUser, Account.AccountType.BANK_ACCOUNT))
                    .thenReturn(accounts);

            // Act
            List<Account> result = accountService.getAccountsByType(Account.AccountType.BANK_ACCOUNT);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Account.AccountType.BANK_ACCOUNT, result.get(0).getType());
            verify(accountRepository).findByUserAndType(testUser, Account.AccountType.BANK_ACCOUNT);
        }
    }

    @Test
    void updateAccountBalance_ShouldUpdateBalanceCorrectly() {
        // Arrange
        BigDecimal newBalance = new BigDecimal("3000.00");
        Account updatedAccount = Account.builder()
                .id(1L)
                .name("Test Account")
                .type(Account.AccountType.BANK_ACCOUNT)
                .balance(newBalance)
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .user(testUser)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

            // Act
            Account result = accountService.updateAccountBalance(1L, newBalance);

            // Assert
            assertNotNull(result);
            assertEquals(newBalance, result.getBalance());
            verify(accountRepository).findByIdAndUser(1L, testUser);
            verify(accountRepository).save(any(Account.class));
        }
    }

    @Test
    void addToAccountBalance_ShouldAddAmountCorrectly() {
        // Arrange
        BigDecimal amountToAdd = new BigDecimal("500.00");
        BigDecimal expectedBalance = new BigDecimal("1500.00");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Account result = accountService.addToAccountBalance(1L, amountToAdd);

            // Assert
            assertNotNull(result);
            assertEquals(expectedBalance, result.getBalance());
            verify(accountRepository).findByIdAndUser(1L, testUser);
            verify(accountRepository).save(testAccount);
        }
    }

    @Test
    void subtractFromAccountBalance_ShouldSubtractAmountCorrectly() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("200.00");
        BigDecimal expectedBalance = new BigDecimal("800.00");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Account result = accountService.subtractFromAccountBalance(1L, amountToSubtract);

            // Assert
            assertNotNull(result);
            assertEquals(expectedBalance, result.getBalance());
            verify(accountRepository).findByIdAndUser(1L, testUser);
            verify(accountRepository).save(testAccount);
        }
    }

    @Test
    void accountExistsByName_WhenAccountExists_ShouldReturnTrue() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.existsByNameAndUser("Test Account", testUser)).thenReturn(true);

            // Act
            boolean result = accountService.accountExistsByName("Test Account");

            // Assert
            assertTrue(result);
            verify(accountRepository).existsByNameAndUser("Test Account", testUser);
        }
    }

    @Test
    void searchAccountsByName_ShouldReturnMatchingAccounts() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount);
        
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(accountRepository.findByUserAndNameContainingIgnoreCase(testUser, "Test"))
                    .thenReturn(accounts);

            // Act
            List<Account> result = accountService.searchAccountsByName("Test");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getName().contains("Test"));
            verify(accountRepository).findByUserAndNameContainingIgnoreCase(testUser, "Test");
        }
    }
}

