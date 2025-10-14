package com.financial.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.dto.TransactionCreateRequestDto;
import com.financial.dto.TransactionDto;
import com.financial.dto.TransactionUpdateRequestDto;
import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.Transaction;
import com.financial.entity.User;
import com.financial.repository.AccountRepository;
import com.financial.repository.CategoryRepository;
import com.financial.repository.TransactionRepository;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TransactionController.
 * Tests all transaction management endpoints with full database integration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private String authToken;
    private Account testAccount;
    private Account testAccount2;
    private Category expenseCategory;
    private Category incomeCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        // Clean up existing test data
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
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

        // Create test accounts
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

        testAccount2 = Account.builder()
                .name("Test Savings Account")
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .includeInBalance(true)
                .user(testUser)
                .build();
        testAccount2 = accountRepository.save(testAccount2);

        // Create test categories
        expenseCategory = Category.builder()
                .name("Food & Dining")
                .description("Food and restaurant expenses")
                .type(Category.CategoryType.EXPENSE)
                .color("#FF5733")
                .icon("restaurant")
                .isActive(true)
                .user(testUser)
                .build();
        expenseCategory = categoryRepository.save(expenseCategory);

        incomeCategory = Category.builder()
                .name("Salary")
                .description("Monthly salary income")
                .type(Category.CategoryType.INCOME)
                .color("#33FF57")
                .icon("money")
                .isActive(true)
                .user(testUser)
                .build();
        incomeCategory = categoryRepository.save(incomeCategory);

        // Create test transaction
        testTransaction = Transaction.builder()
                .description("Test Expense")
                .amount(new BigDecimal("50.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .account(testAccount)
                .category(expenseCategory)
                .transactionDate(LocalDateTime.now())
                .notes("Test notes")
                .location("Test Location")
                .referenceNumber("REF-001")
                .isRecurring(false)
                .user(testUser)
                .build();
        testTransaction = transactionRepository.save(testTransaction);
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Create Transaction Tests ==========

    @Test
    void createTransaction_WithValidExpense_ShouldReturnCreated() throws Exception {
        TransactionCreateRequestDto dto = TransactionCreateRequestDto.builder()
                .description("Grocery Shopping")
                .amount(new BigDecimal("150.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(testAccount.getId())
                .categoryId(expenseCategory.getId())
                .transactionDate(LocalDateTime.now())
                .notes("Weekly groceries")
                .location("Supermarket")
                .build();

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Grocery Shopping"))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.accountId").value(testAccount.getId()))
                .andExpect(jsonPath("$.categoryId").value(expenseCategory.getId()));
    }

    @Test
    void createTransaction_WithValidIncome_ShouldReturnCreated() throws Exception {
        TransactionCreateRequestDto dto = TransactionCreateRequestDto.builder()
                .description("Monthly Salary")
                .amount(new BigDecimal("5000.00"))
                .type(Transaction.TransactionType.INCOME)
                .accountId(testAccount.getId())
                .categoryId(incomeCategory.getId())
                .transactionDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Monthly Salary"))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    void createTransaction_WithTransfer_ShouldReturnCreated() throws Exception {
        TransactionCreateRequestDto dto = TransactionCreateRequestDto.builder()
                .description("Transfer to Savings")
                .amount(new BigDecimal("1000.00"))
                .type(Transaction.TransactionType.TRANSFER)
                .accountId(testAccount.getId())
                .transferToAccountId(testAccount2.getId())
                .transactionDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Transfer to Savings"))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.transferToAccountId").value(testAccount2.getId()));
    }

    @Test
    void createTransaction_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        TransactionDto dto = TransactionDto.builder()
                .description("Test Transaction")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(testAccount.getId())
                .transactionDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTransaction_WithInvalidAccount_ShouldReturnNotFound() throws Exception {
        TransactionCreateRequestDto dto = TransactionCreateRequestDto.builder()
                .description("Test Transaction")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(99999L) // Non-existent account
                .transactionDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_WithInvalidCategory_ShouldReturnNotFound() throws Exception {
        TransactionCreateRequestDto dto = TransactionCreateRequestDto.builder()
                .description("Test Transaction")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(testAccount.getId())
                .categoryId(99999L) // Non-existent category
                .transactionDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_WithRecurringFlag_ShouldReturnCreated() throws Exception {
        TransactionCreateRequestDto dto = TransactionCreateRequestDto.builder()
                .description("Recurring Bill")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(testAccount.getId())
                .categoryId(expenseCategory.getId())
                .transactionDate(LocalDateTime.now())
                .isRecurring(true)
                .recurringFrequency(Transaction.RecurringFrequency.MONTHLY)
                .recurringEndDate(LocalDateTime.now().plusYears(1))
                .build();

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isRecurring").value(true))
                .andExpect(jsonPath("$.recurringFrequency").value("MONTHLY"));
    }

    // ========== Get Transaction by ID Tests ==========

    @Test
    void getTransactionById_WithExistingId_ShouldReturnTransaction() throws Exception {
        mockMvc.perform(get("/api/transactions/" + testTransaction.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTransaction.getId()))
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    void getTransactionById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/99999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionById_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions/" + testTransaction.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ========== Get All Transactions Tests ==========

    @Test
    void getAllTransactions_WithPagination_ShouldReturnPage() throws Exception {
        // Create additional transactions
        for (int i = 1; i <= 5; i++) {
            Transaction transaction = Transaction.builder()
                    .description("Transaction " + i)
                    .amount(new BigDecimal(100.00 * i))
                    .type(Transaction.TransactionType.EXPENSE)
                    .account(testAccount)
                    .category(expenseCategory)
                    .transactionDate(LocalDateTime.now().minusDays(i))
                    .user(testUser)
                    .build();
            transactionRepository.save(transaction);
        }

        mockMvc.perform(get("/api/transactions")
                        .param("page", "0")
                        .param("size", "3")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(6)) // 5 new + 1 from setup
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getAllTransactions_WithSorting_ShouldReturnSortedResults() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .param("sort", "amount,asc")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllTransactions_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    // ========== Get Last Transactions Tests ==========

    @Test
    void getLast5Transactions_ShouldReturnLastFive() throws Exception {
        // Create additional transactions
        for (int i = 1; i <= 7; i++) {
            Transaction transaction = Transaction.builder()
                    .description("Transaction " + i)
                    .amount(new BigDecimal(100.00))
                    .type(Transaction.TransactionType.EXPENSE)
                    .account(testAccount)
                    .transactionDate(LocalDateTime.now().minusHours(i))
                    .user(testUser)
                    .build();
            transactionRepository.save(transaction);
        }

        mockMvc.perform(get("/api/transactions/last-5")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(5)));
    }

    @Test
    void getLastTransactions_WithCustomLimit_ShouldReturnCorrectNumber() throws Exception {
        mockMvc.perform(get("/api/transactions/last")
                        .param("limit", "3")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(3)));
    }

    // ========== Get Transactions by Account Tests ==========

    @Test
    void getTransactionsByAccount_WithExistingAccount_ShouldReturnTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions/account/" + testAccount.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountId").value(testAccount.getId()));
    }

    @Test
    void getTransactionsByAccount_WithNonExistingAccount_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/account/99999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionsByAccount_WithPagination_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/transactions/account/" + testAccount.getId() + "/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getLastTransactionsByAccount_WithLimit_ShouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/transactions/account/" + testAccount.getId() + "/last")
                        .param("limit", "5")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ========== Get Transactions by Type Tests ==========

    @Test
    void getTransactionsByType_WithValidType_ShouldReturnMatchingTransactions() throws Exception {
        // Create income transaction
        Transaction incomeTransaction = Transaction.builder()
                .description("Income Transaction")
                .amount(new BigDecimal("2000.00"))
                .type(Transaction.TransactionType.INCOME)
                .account(testAccount)
                .category(incomeCategory)
                .transactionDate(LocalDateTime.now())
                .user(testUser)
                .build();
        transactionRepository.save(incomeTransaction);

        mockMvc.perform(get("/api/transactions/type/EXPENSE")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].type").value(everyItem(is("EXPENSE"))));
    }

    @Test
    void getTransactionsByType_WithInvalidType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/transactions/type/INVALID_TYPE")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionsByType_CaseInsensitive_ShouldWork() throws Exception {
        mockMvc.perform(get("/api/transactions/type/expense")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ========== Get Transactions by Category Tests ==========

    @Test
    void getTransactionsByCategory_WithExistingCategory_ShouldReturnTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions/category/" + expenseCategory.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].categoryId").value(expenseCategory.getId()));
    }

    @Test
    void getTransactionsByCategory_WithNonExistingCategory_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/category/99999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    // ========== Get Transactions by Date Range Tests ==========

    @Test
    void getTransactionsByDateRange_WithValidDates_ShouldReturnResults() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        mockMvc.perform(get("/api/transactions/date-range")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getTransactionsByDateRange_WithFutureDates_ShouldReturnEmptyList() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(20);

        mockMvc.perform(get("/api/transactions/date-range")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ========== Get Transactions by Amount Range Tests ==========

    @Test
    void getTransactionsByAmountRange_WithValidRange_ShouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/transactions/amount-range")
                        .param("minAmount", "0")
                        .param("maxAmount", "100")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].amount").value(lessThanOrEqualTo(100.0)));
    }

    @Test
    void getTransactionsByAmountRange_WithNoMatches_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/transactions/amount-range")
                        .param("minAmount", "10000")
                        .param("maxAmount", "20000")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ========== Search Transactions Tests ==========

    @Test
    void searchTransactionsByDescription_WithMatchingPattern_ShouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/transactions/search")
                        .param("description", "Test")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].description").value(containsString("Test")));
    }

    @Test
    void searchTransactionsByDescription_CaseInsensitive_ShouldWork() throws Exception {
        mockMvc.perform(get("/api/transactions/search")
                        .param("description", "test")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchTransactionsByDescription_WithNoMatches_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/transactions/search")
                        .param("description", "NonExistentDescription")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ========== Update Transaction Tests ==========

    @Test
    void updateTransaction_WithValidData_ShouldReturnUpdated() throws Exception {
        TransactionUpdateRequestDto dto = TransactionUpdateRequestDto.builder()
                .description("Updated Description")
                .amount(new BigDecimal("75.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(testAccount.getId())
                .categoryId(expenseCategory.getId())
                .transactionDate(LocalDateTime.now())
                .notes("Updated notes")
                .build();

        mockMvc.perform(put("/api/transactions/" + testTransaction.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.amount").value(75.00))
                .andExpect(jsonPath("$.notes").value("Updated notes"));
    }

    @Test
    void updateTransaction_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        TransactionUpdateRequestDto dto = TransactionUpdateRequestDto.builder()
                .description("Updated Description")
                .amount(new BigDecimal("75.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(testAccount.getId())
                .transactionDate(LocalDateTime.now())
                .build();

        mockMvc.perform(put("/api/transactions/99999")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTransaction_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        TransactionDto dto = TransactionDto.builder()
                .description("Updated Description")
                .amount(new BigDecimal("75.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(testAccount.getId())
                .transactionDate(LocalDateTime.now())
                .build();

        mockMvc.perform(put("/api/transactions/" + testTransaction.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ========== Delete Transaction Tests ==========

    @Test
    void deleteTransaction_WithExistingId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/transactions/" + testTransaction.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify transaction is deleted
        mockMvc.perform(get("/api/transactions/" + testTransaction.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTransaction_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/transactions/99999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTransaction_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/transactions/" + testTransaction.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ========== Get Total Amount by Type Tests ==========

    @Test
    void getTotalAmountByType_WithExpenses_ShouldCalculateCorrectTotal() throws Exception {
        // Create additional expense
        Transaction additionalExpense = Transaction.builder()
                .description("Another Expense")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .account(testAccount)
                .transactionDate(LocalDateTime.now())
                .user(testUser)
                .build();
        transactionRepository.save(additionalExpense);

        mockMvc.perform(get("/api/transactions/total-amount/type/EXPENSE")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(150.00)); // 50 + 100
    }

    @Test
    void getTotalAmountByType_WithInvalidType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/transactions/total-amount/type/INVALID")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    // ========== Get Total Income/Expense by Account Tests ==========

    @Test
    void getTotalIncomeByAccount_ShouldCalculateCorrectTotal() throws Exception {
        // Create income transaction
        Transaction income = Transaction.builder()
                .description("Income")
                .amount(new BigDecimal("3000.00"))
                .type(Transaction.TransactionType.INCOME)
                .account(testAccount)
                .transactionDate(LocalDateTime.now())
                .user(testUser)
                .build();
        transactionRepository.save(income);

        mockMvc.perform(get("/api/transactions/account/" + testAccount.getId() + "/total-income")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3000.00));
    }

    @Test
    void getTotalExpenseByAccount_ShouldCalculateCorrectTotal() throws Exception {
        mockMvc.perform(get("/api/transactions/account/" + testAccount.getId() + "/total-expense")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(50.00));
    }

    @Test
    void getTotalIncomeByAccount_WithNonExistingAccount_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/account/99999/total-income")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    // ========== User Isolation Tests ==========

    @Test
    void getAllTransactions_ShouldOnlyReturnCurrentUserTransactions() throws Exception {
        // Create another user with their own transaction
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
        otherUserAccount = accountRepository.save(otherUserAccount);

        Transaction otherUserTransaction = Transaction.builder()
                .description("Other User Transaction")
                .amount(new BigDecimal("200.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .account(otherUserAccount)
                .transactionDate(LocalDateTime.now())
                .user(otherUser)
                .build();
        transactionRepository.save(otherUserTransaction);

        // Test that current user only sees their own transactions
        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.description=='Test Expense')]").exists())
                .andExpect(jsonPath("$.content[?(@.description=='Other User Transaction')]").doesNotExist());
    }

    // ========== Complex Scenario Tests ==========

    @Test
    void createMultipleTransactions_AndCalculateTotals_ShouldWorkCorrectly() throws Exception {
        // Create multiple transactions of different types
        TransactionCreateRequestDto income1 = TransactionCreateRequestDto.builder()
                .description("Salary")
                .amount(new BigDecimal("5000.00"))
                .type(Transaction.TransactionType.INCOME)
                .accountId(testAccount.getId())
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionCreateRequestDto expense1 = TransactionCreateRequestDto.builder()
                .description("Rent")
                .amount(new BigDecimal("1500.00"))
                .type(Transaction.TransactionType.EXPENSE)
                .accountId(testAccount.getId())
                .transactionDate(LocalDateTime.now())
                .build();

        // Create transactions
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(income1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense1)))
                .andExpect(status().isCreated());

        // Verify totals
        mockMvc.perform(get("/api/transactions/account/" + testAccount.getId() + "/total-income")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5000.00));

        mockMvc.perform(get("/api/transactions/account/" + testAccount.getId() + "/total-expense")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(greaterThanOrEqualTo(1500.0))); // Including testTransaction
    }
}

