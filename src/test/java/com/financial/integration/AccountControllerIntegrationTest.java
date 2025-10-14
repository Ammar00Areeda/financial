package com.financial.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.dto.AccountUpdateRequestDto;
import com.financial.entity.Account;
import com.financial.entity.User;
import com.financial.repository.AccountRepository;
import com.financial.repository.UserRepository;
import com.financial.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AccountController.
 * Tests all account management endpoints with full database integration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private String authToken;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Clean up existing test data
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        testUser = userRepository.save(testUser);

        // Generate JWT token
        authToken = jwtUtil.generateToken(testUser);

        // Create test account
        testAccount = Account.builder()
                .name("Test Bank Account")
                .type(Account.AccountType.BANK_ACCOUNT)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        testAccount = accountRepository.save(testAccount);
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Create Account Tests ==========

    @Test
    void createAccount_WithValidData_ShouldReturnCreated() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Savings Account");
        request.put("type", "SAVINGS");
        request.put("initialBalance", 5000.00);
        request.put("currency", "USD");

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Savings Account"))
                .andExpect(jsonPath("$.type").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createAccount_WithMinimalData_ShouldReturnCreated() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Wallet");
        request.put("type", "WALLET");
        request.put("initialBalance", 100.00);
        request.put("currency", "JOD");

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Wallet"))
                .andExpect(jsonPath("$.type").value("WALLET"))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void createAccount_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Account");
        request.put("type", "BANK_ACCOUNT");
        request.put("initialBalance", 1000.00);
        request.put("currency", "USD");

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAccount_WithInvalidType_ShouldReturnBadRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Account");
        request.put("type", "INVALID_TYPE");
        request.put("initialBalance", 1000.00);
        request.put("currency", "USD");

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Due to invalid enum value
    }

    // ========== Get All Accounts Tests ==========

    @Test
    void getAllAccounts_WithExistingAccounts_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").value(testAccount.getId().toString()))
                .andExpect(jsonPath("$[0].name").value("Test Bank Account"))
                .andExpect(jsonPath("$[0].balance").value(1000.00));
    }

    @Test
    void getAllAccounts_WithNoAccounts_ShouldReturnEmptyList() throws Exception {
        accountRepository.deleteAll();

        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllAccounts_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    // ========== Get Account by ID Tests ==========

    @Test
    void getAccountById_WithExistingId_ShouldReturnAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/" + testAccount.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccount.getId()))
                .andExpect(jsonPath("$.name").value("Test Bank Account"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.type").value("BANK_ACCOUNT"));
    }

    @Test
    void getAccountById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/accounts/99999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccountById_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/accounts/" + testAccount.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ========== Get Active Accounts Tests ==========

    @Test
    void getActiveAccounts_ShouldReturnOnlyActiveAccounts() throws Exception {
        // Create inactive account
        Account inactiveAccount = Account.builder()
                .name("Inactive Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .currency("USD")
                .status(Account.AccountStatus.INACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        accountRepository.save(inactiveAccount);

        mockMvc.perform(get("/api/accounts/active")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name=='Test Bank Account')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Inactive Account')]").doesNotExist());
    }

    // ========== Get Accounts by Type Tests ==========

    @Test
    void getAccountsByType_WithValidType_ShouldReturnMatchingAccounts() throws Exception {
        // Create account with different type
        Account savingsAccount = Account.builder()
                .name("Savings Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        accountRepository.save(savingsAccount);

        mockMvc.perform(get("/api/accounts/type/BANK_ACCOUNT")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("BANK_ACCOUNT"))
                .andExpect(jsonPath("$[?(@.type=='SAVINGS')]").doesNotExist());
    }

    @Test
    void getAccountsByType_WithInvalidType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/accounts/type/INVALID_TYPE")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccountsByType_CaseInsensitive_ShouldWork() throws Exception {
        mockMvc.perform(get("/api/accounts/type/bank_account")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ========== Get Active Accounts by Type Tests ==========

    @Test
    void getActiveAccountsByType_ShouldReturnOnlyActiveAccountsOfType() throws Exception {
        // Create inactive account of same type
        Account inactiveAccount = Account.builder()
                .name("Inactive Bank Account")
                .type(Account.AccountType.BANK_ACCOUNT)
                .balance(new BigDecimal("2000.00"))
                .currency("USD")
                .status(Account.AccountStatus.INACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        accountRepository.save(inactiveAccount);

        mockMvc.perform(get("/api/accounts/active/type/BANK_ACCOUNT")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name=='Test Bank Account')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Inactive Bank Account')]").doesNotExist());
    }

    // ========== Get Accounts Included in Balance Tests ==========

    @Test
    void getAccountsIncludedInBalance_ShouldReturnOnlyIncludedAccounts() throws Exception {
        // Create account not included in balance
        Account excludedAccount = Account.builder()
                .name("Excluded Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("3000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(false)
                .user(testUser)
                .build();
        accountRepository.save(excludedAccount);

        mockMvc.perform(get("/api/accounts/included-in-balance")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name=='Test Bank Account')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Excluded Account')]").doesNotExist());
    }

    // ========== Get Total Balance Tests ==========

    @Test
    void getTotalBalance_ShouldCalculateCorrectTotal() throws Exception {
        // Create additional account
        Account additionalAccount = Account.builder()
                .name("Additional Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        accountRepository.save(additionalAccount);

        mockMvc.perform(get("/api/accounts/total-balance")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3000.00)); // 1000 + 2000
    }

    @Test
    void getTotalBalance_WithExcludedAccount_ShouldExcludeFromTotal() throws Exception {
        // Create account excluded from balance
        Account excludedAccount = Account.builder()
                .name("Excluded Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(false)
                .user(testUser)
                .build();
        accountRepository.save(excludedAccount);

        mockMvc.perform(get("/api/accounts/total-balance")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1000.00)); // Only testAccount
    }

    // ========== Get Total Balance by Type Tests ==========

    @Test
    void getTotalBalanceByType_ShouldCalculateCorrectTotal() throws Exception {
        // Create additional bank account
        Account additionalBankAccount = Account.builder()
                .name("Second Bank Account")
                .type(Account.AccountType.BANK_ACCOUNT)
                .balance(new BigDecimal("3000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        accountRepository.save(additionalBankAccount);

        // Create savings account (should not be included)
        Account savingsAccount = Account.builder()
                .name("Savings Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        accountRepository.save(savingsAccount);

        mockMvc.perform(get("/api/accounts/total-balance/type/BANK_ACCOUNT")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4000.00)); // 1000 + 3000
    }

    // ========== Search Accounts Tests ==========

    @Test
    void searchAccountsByName_WithMatchingPattern_ShouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/accounts/search")
                        .param("name", "Test")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name").value(containsString("Test")));
    }

    @Test
    void searchAccountsByName_CaseInsensitive_ShouldWork() throws Exception {
        mockMvc.perform(get("/api/accounts/search")
                        .param("name", "test")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void searchAccountsByName_WithNoMatches_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/accounts/search")
                        .param("name", "NonExistentAccount")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========== Search Active Accounts Tests ==========

    @Test
    void searchActiveAccountsByName_ShouldReturnOnlyActiveMatches() throws Exception {
        // Create inactive account with matching name
        Account inactiveAccount = Account.builder()
                .name("Test Inactive Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .currency("USD")
                .status(Account.AccountStatus.INACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        accountRepository.save(inactiveAccount);

        mockMvc.perform(get("/api/accounts/active/search")
                        .param("name", "Test")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name=='Test Bank Account')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Test Inactive Account')]").doesNotExist());
    }

    // ========== Update Account Tests ==========

    @Test
    void updateAccount_WithValidData_ShouldReturnUpdated() throws Exception {
        AccountUpdateRequestDto updateData = AccountUpdateRequestDto.builder()
                .name("Updated Account Name")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("2500.00"))
                .currency("EUR")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .bankName("Updated Bank")
                .build();

        mockMvc.perform(put("/api/accounts/" + testAccount.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Account Name"))
                .andExpect(jsonPath("$.type").value("SAVINGS"))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void updateAccount_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        AccountUpdateRequestDto updateData = AccountUpdateRequestDto.builder()
                .name("Updated Account Name")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("2500.00"))
                .currency("USD")
                .build();

        mockMvc.perform(put("/api/accounts/99999")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound()); // Non-existing ID returns 404
    }

    @Test
    void updateAccount_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        AccountUpdateRequestDto updateData = AccountUpdateRequestDto.builder()
                .name("Updated Account Name")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("2500.00"))
                .currency("USD")
                .build();

        mockMvc.perform(put("/api/accounts/" + testAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isUnauthorized());
    }

    // ========== Update Account Balance Tests ==========

    @Test
    void updateAccountBalance_WithValidAmount_ShouldReturnUpdated() throws Exception {
        mockMvc.perform(patch("/api/accounts/" + testAccount.getId() + "/balance")
                        .param("balance", "5000.00")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    void updateAccountBalance_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(patch("/api/accounts/99999/balance")
                        .param("balance", "5000.00")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    // ========== Add to Account Balance Tests ==========

    @Test
    void addToAccountBalance_WithValidAmount_ShouldIncreaseBalance() throws Exception {
        mockMvc.perform(patch("/api/accounts/" + testAccount.getId() + "/add")
                        .param("amount", "500.00")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500.00)); // 1000 + 500
    }

    @Test
    void addToAccountBalance_WithNegativeAmount_ShouldStillWork() throws Exception {
        mockMvc.perform(patch("/api/accounts/" + testAccount.getId() + "/add")
                        .param("amount", "-200.00")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.00)); // 1000 - 200
    }

    // ========== Subtract from Account Balance Tests ==========

    @Test
    void subtractFromAccountBalance_WithValidAmount_ShouldDecreaseBalance() throws Exception {
        mockMvc.perform(patch("/api/accounts/" + testAccount.getId() + "/subtract")
                        .param("amount", "300.00")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(700.00)); // 1000 - 300
    }

    @Test
    void subtractFromAccountBalance_ExceedingBalance_ShouldAllowNegativeBalance() throws Exception {
        mockMvc.perform(patch("/api/accounts/" + testAccount.getId() + "/subtract")
                        .param("amount", "1500.00")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-500.00)); // 1000 - 1500
    }

    // ========== Delete Account Tests ==========

    @Test
    void deleteAccount_WithExistingId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/accounts/" + testAccount.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify account is deleted
        mockMvc.perform(get("/api/accounts/" + testAccount.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAccount_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/accounts/99999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAccount_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/accounts/" + testAccount.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ========== User Isolation Tests ==========

    @Test
    void getAllAccounts_ShouldOnlyReturnCurrentUserAccounts() throws Exception {
        // Create another user with their own account
        User otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Other")
                .lastName("User")
                .role(User.Role.USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        otherUser = userRepository.save(otherUser);

        Account otherUserAccount = Account.builder()
                .name("Other User Account")
                .type(Account.AccountType.WALLET)
                .balance(new BigDecimal("500.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .user(otherUser)
                .build();
        accountRepository.save(otherUserAccount);

        // Test that current user only sees their own accounts
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name=='Test Bank Account')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Other User Account')]").doesNotExist());
    }
}

