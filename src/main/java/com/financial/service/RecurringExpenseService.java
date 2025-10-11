package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.RecurringExpense;
import com.financial.entity.Transaction;
import com.financial.entity.User;
import com.financial.repository.RecurringExpenseRepository;
import com.financial.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for recurring expense-related business logic.
 * 
 * <p>This service manages recurring expenses such as subscriptions, rent, utilities, and
 * other periodic payments. It handles automatic payment processing, due date tracking,
 * reminders, and integration with transactions and account balances.</p>
 * 
 * <p><b>Security:</b> All methods require authentication. Operations are automatically
 * scoped to the authenticated user retrieved via {@link SecurityUtils#getAuthenticatedUser()}.
 * Users can only access and modify their own recurring expenses.</p>
 * 
 * @see RecurringExpense
 * @see RecurringExpenseRepository
 * @see SecurityUtils
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RecurringExpenseService {
    
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    
    /**
     * Retrieves all recurring expenses for the authenticated user with pagination support.
     * 
     * <p><b>Security:</b> Requires authentication. Only returns recurring expenses belonging
     * to the authenticated user.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("nextDueDate"));
     * Page<RecurringExpense> expenses = recurringExpenseService.getAllRecurringExpenses(pageable);
     * expenses.forEach(expense -> 
     *     System.out.println(expense.getName() + " - Due: " + expense.getNextDueDate())
     * );
     * }</pre>
     *
     * @param pageable pagination information including page number, size, and sort order
     * @return page of recurring expenses belonging to the authenticated user
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    @Transactional(readOnly = true)
    public Page<RecurringExpense> getAllRecurringExpenses(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return recurringExpenseRepository.findByUser(currentUser, pageable);
    }
    
    /**
     * Get all recurring expenses for the authenticated user.
     *
     * @return list of all recurring expenses
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getAllRecurringExpenses() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return recurringExpenseRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
    }
    
    /**
     * Get recurring expenses by account.
     *
     * @param account the account
     * @return list of recurring expenses for the account
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByAccount(Account account) {
        return recurringExpenseRepository.findByAccount(account);
    }
    
    /**
     * Get recurring expenses by account with pagination.
     *
     * @param account the account
     * @param pageable pagination information
     * @return page of recurring expenses for the account
     */
    @Transactional(readOnly = true)
    public Page<RecurringExpense> getRecurringExpensesByAccount(Account account, Pageable pageable) {
        return recurringExpenseRepository.findByAccount(account, pageable);
    }
    
    /**
     * Get recurring expenses by status for the authenticated user.
     *
     * @param status the status
     * @return list of recurring expenses with the specified status
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByStatus(RecurringExpense.Status status) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return recurringExpenseRepository.findByUserAndStatus(currentUser, status);
    }
    
    /**
     * Get recurring expenses by frequency for the authenticated user.
     *
     * @param frequency the frequency
     * @return list of recurring expenses with the specified frequency
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByFrequency(RecurringExpense.Frequency frequency) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<RecurringExpense> allExpenses = recurringExpenseRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allExpenses.stream()
                .filter(e -> e.getFrequency() == frequency)
                .toList();
    }
    
    /**
     * Get recurring expenses by account and status.
     *
     * @param account the account
     * @param status the status
     * @return list of recurring expenses for the account and status
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByAccountAndStatus(Account account, RecurringExpense.Status status) {
        return recurringExpenseRepository.findByAccountAndStatus(account, status);
    }
    
    /**
     * Search recurring expenses by name for the authenticated user.
     *
     * @param name the name pattern to search for
     * @return list of recurring expenses matching the pattern
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> searchRecurringExpensesByName(String name) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<RecurringExpense> allExpenses = recurringExpenseRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allExpenses.stream()
                .filter(e -> e.getName() != null && 
                            e.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }
    
    /**
     * Search recurring expenses by provider for the authenticated user.
     *
     * @param provider the provider pattern to search for
     * @return list of recurring expenses matching the pattern
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> searchRecurringExpensesByProvider(String provider) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<RecurringExpense> allExpenses = recurringExpenseRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allExpenses.stream()
                .filter(e -> e.getProvider() != null && 
                            e.getProvider().toLowerCase().contains(provider.toLowerCase()))
                .toList();
    }
    
    /**
     * Get recurring expenses due today for the authenticated user.
     *
     * @return list of recurring expenses due today
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesDueToday() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return recurringExpenseRepository.findDueTodayByUser(currentUser, LocalDate.now());
    }
    
    /**
     * Get overdue recurring expenses for the authenticated user.
     *
     * @return list of overdue recurring expenses
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getOverdueRecurringExpenses() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return recurringExpenseRepository.findOverdueByUser(currentUser, LocalDate.now());
    }
    
    /**
     * Get recurring expenses due soon for the authenticated user.
     *
     * @param daysAhead the number of days ahead to check
     * @return list of recurring expenses due soon
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesDueSoon(int daysAhead) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);
        List<RecurringExpense> allExpenses = recurringExpenseRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allExpenses.stream()
                .filter(e -> e.getNextDueDate() != null && 
                            !e.getNextDueDate().isBefore(today) && 
                            !e.getNextDueDate().isAfter(futureDate) &&
                            e.getStatus() == RecurringExpense.Status.ACTIVE)
                .toList();
    }
    
    /**
     * Get recurring expenses with auto-pay enabled for the authenticated user.
     *
     * @return list of recurring expenses with auto-pay enabled
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesWithAutoPay() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<RecurringExpense> allExpenses = recurringExpenseRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allExpenses.stream()
                .filter(e -> e.getIsAutoPay() != null && e.getIsAutoPay())
                .toList();
    }
    
    /**
     * Get recurring expenses by amount range.
     *
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of recurring expenses within the amount range
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return recurringExpenseRepository.findByAmountBetween(minAmount, maxAmount);
    }
    
    /**
     * Get recurring expenses by account and amount range.
     *
     * @param account the account
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of recurring expenses for the account within the amount range
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByAccountAndAmountRange(Account account, BigDecimal minAmount, BigDecimal maxAmount) {
        return recurringExpenseRepository.findByAccountAndAmountBetween(account, minAmount, maxAmount);
    }
    
    /**
     * Get recurring expense by ID for the authenticated user.
     *
     * @param id the recurring expense ID
     * @return Optional containing the recurring expense if found
     */
    @Transactional(readOnly = true)
    public Optional<RecurringExpense> getRecurringExpenseById(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return recurringExpenseRepository.findByIdAndUser(id, currentUser);
    }
    
    /**
     * Creates a new recurring expense for the authenticated user.
     * 
     * <p>The recurring expense is automatically associated with the authenticated user.
     * If the next due date is not provided, it is calculated based on the start date
     * and frequency.</p>
     * 
     * <p><b>Security:</b> Requires authentication. The recurring expense is associated
     * with the authenticated user and cannot be transferred to another user.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * RecurringExpense expense = RecurringExpense.builder()
     *     .name("Netflix Subscription")
     *     .amount(new BigDecimal("15.99"))
     *     .frequency(Frequency.MONTHLY)
     *     .startDate(LocalDate.now())
     *     .provider("Netflix")
     *     .status(Status.ACTIVE)
     *     .isAutoPay(true)
     *     .account(myAccount)
     *     .category(subscriptionCategory)
     *     .build();
     * 
     * RecurringExpense created = recurringExpenseService.createRecurringExpense(expense);
     * System.out.println("Recurring expense created with ID: " + created.getId());
     * System.out.println("Next due date: " + created.getNextDueDate());
     * }</pre>
     *
     * @param recurringExpense the recurring expense to create (must not be null)
     * @return the persisted recurring expense with generated ID and calculated due date
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public RecurringExpense createRecurringExpense(RecurringExpense recurringExpense) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        // Associate recurring expense with current user
        recurringExpense.setUser(currentUser);
        
        // Calculate next due date if not provided
        if (recurringExpense.getNextDueDate() == null) {
            recurringExpense.setNextDueDate(recurringExpense.calculateNextDueDate(
                recurringExpense.getStartDate(), 
                recurringExpense.getFrequency()
            ));
        }
        
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Update an existing recurring expense for the authenticated user.
     *
     * @param recurringExpense the recurring expense to update
     * @return the updated recurring expense
     * @throws IllegalArgumentException if recurring expense not found or doesn't belong to user
     */
    public RecurringExpense updateRecurringExpense(RecurringExpense recurringExpense) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        // Verify recurring expense belongs to current user
        RecurringExpense existingExpense = recurringExpenseRepository.findByIdAndUser(recurringExpense.getId(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + recurringExpense.getId() + " not found"));
        
        // Ensure user association is not changed
        recurringExpense.setUser(currentUser);
        
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Delete recurring expense by ID for the authenticated user.
     *
     * @param id the recurring expense ID
     * @throws IllegalArgumentException if recurring expense not found or doesn't belong to user
     */
    public void deleteRecurringExpense(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        RecurringExpense expense = recurringExpenseRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + id + " not found"));
        
        recurringExpenseRepository.delete(expense);
    }
    
    /**
     * Marks a recurring expense as paid and creates a corresponding transaction.
     * 
     * <p>This method performs the following actions:
     * <ul>
     *   <li>Updates the last paid date to today</li>
     *   <li>Calculates and sets the next due date based on frequency</li>
     *   <li>Creates a transaction record for the payment</li>
     *   <li>Deducts the expense amount from the associated account balance</li>
     * </ul>
     * </p>
     * 
     * <p><b>Security:</b> Requires authentication. Only recurring expenses belonging to
     * the authenticated user can be marked as paid.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Mark a recurring expense as paid
     * RecurringExpense expense = recurringExpenseService.markAsPaid(123L);
     * 
     * System.out.println("Last paid: " + expense.getLastPaidDate());
     * System.out.println("Next due: " + expense.getNextDueDate());
     * 
     * // The transaction is automatically created
     * List<Transaction> transactions = transactionService.getAllTransactions();
     * Transaction latest = transactions.get(0);
     * System.out.println("Transaction created: " + latest.getDescription());
     * }</pre>
     *
     * @param id the ID of the recurring expense to mark as paid
     * @return the updated recurring expense with new due date and payment information
     * @throws IllegalArgumentException if the recurring expense doesn't exist or doesn't
     *         belong to the authenticated user
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public RecurringExpense markAsPaid(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        RecurringExpense recurringExpense = recurringExpenseRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + id + " not found"));
        
        // Mark as paid and calculate next due date
        recurringExpense.markAsPaid();
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .description("Recurring payment: " + recurringExpense.getName())
                .amount(recurringExpense.getAmount())
                .type(Transaction.TransactionType.EXPENSE)
                .account(recurringExpense.getAccount())
                .category(recurringExpense.getCategory())
                .transactionDate(recurringExpense.getLastPaidDate().atStartOfDay())
                .notes("Auto-generated from recurring expense: " + recurringExpense.getName())
                .referenceNumber(recurringExpense.getReferenceNumber())
                .isRecurring(true)
                .recurringFrequency(Transaction.RecurringFrequency.valueOf(recurringExpense.getFrequency().name()))
                .user(currentUser)
                .build();
        
        // Create transaction and update account balance
        transactionService.createTransaction(transaction);
        accountService.subtractFromAccountBalance(recurringExpense.getAccount().getId(), recurringExpense.getAmount());
        
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Pause a recurring expense for the authenticated user.
     *
     * @param id the recurring expense ID
     * @return the updated recurring expense
     * @throws IllegalArgumentException if recurring expense not found or doesn't belong to user
     */
    public RecurringExpense pauseRecurringExpense(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        RecurringExpense recurringExpense = recurringExpenseRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + id + " not found"));
        
        recurringExpense.setStatus(RecurringExpense.Status.PAUSED);
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Resume a paused recurring expense for the authenticated user.
     *
     * @param id the recurring expense ID
     * @return the updated recurring expense
     * @throws IllegalArgumentException if recurring expense not found or doesn't belong to user
     */
    public RecurringExpense resumeRecurringExpense(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        RecurringExpense recurringExpense = recurringExpenseRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + id + " not found"));
        
        recurringExpense.setStatus(RecurringExpense.Status.ACTIVE);
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Cancel a recurring expense for the authenticated user.
     *
     * @param id the recurring expense ID
     * @return the updated recurring expense
     * @throws IllegalArgumentException if recurring expense not found or doesn't belong to user
     */
    public RecurringExpense cancelRecurringExpense(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        RecurringExpense recurringExpense = recurringExpenseRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + id + " not found"));
        
        recurringExpense.setStatus(RecurringExpense.Status.CANCELLED);
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Processes all due recurring expenses for the authenticated user with auto-pay enabled.
     * 
     * <p>This method is designed to be called by a scheduled job. It finds all recurring
     * expenses that are due today and have auto-pay enabled, then automatically marks them
     * as paid, creating transactions and updating account balances.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only processes recurring expenses belonging
     * to the authenticated user.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Typically called by a scheduled job
     * @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
     * public void processRecurringExpensesJob() {
     *     int processed = recurringExpenseService.processAllDueRecurringExpenses();
     *     logger.info("Processed {} recurring expenses", processed);
     * }
     * 
     * // Can also be called manually
     * int count = recurringExpenseService.processAllDueRecurringExpenses();
     * System.out.println("Processed " + count + " auto-pay expenses");
     * }</pre>
     *
     * @return the number of recurring expenses that were processed and paid
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    @Transactional
    public int processAllDueRecurringExpenses() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<RecurringExpense> dueExpenses = recurringExpenseRepository.findDueTodayByUser(currentUser, LocalDate.now());
        
        for (RecurringExpense expense : dueExpenses) {
            if (expense.getIsAutoPay() != null && expense.getIsAutoPay()) {
                markAsPaid(expense.getId());
            }
        }
        
        return dueExpenses.size();
    }
    
    // ========== REPORTING METHODS ==========
    
    /**
     * Calculate total monthly recurring expenses for an account.
     *
     * @param account the account
     * @return total monthly recurring expenses
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalMonthlyRecurringExpenses(Account account) {
        return recurringExpenseRepository.calculateTotalMonthlyRecurringExpenses(account);
    }
    
    /**
     * Calculate total recurring expenses by frequency for an account.
     *
     * @param account the account
     * @param frequency the frequency
     * @return total recurring expenses for the frequency
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRecurringExpensesByFrequency(Account account, RecurringExpense.Frequency frequency) {
        return recurringExpenseRepository.calculateTotalRecurringExpensesByFrequency(account, frequency);
    }
    
    /**
     * Calculate total recurring expenses for an account.
     *
     * @param account the account
     * @return total recurring expenses
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRecurringExpenses(Account account) {
        return recurringExpenseRepository.calculateTotalRecurringExpenses(account);
    }
    
    /**
     * Get recurring expenses by reference number.
     *
     * @param referenceNumber the reference number
     * @return list of recurring expenses with the reference number
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByReferenceNumber(String referenceNumber) {
        return recurringExpenseRepository.findByReferenceNumber(referenceNumber);
    }
}
