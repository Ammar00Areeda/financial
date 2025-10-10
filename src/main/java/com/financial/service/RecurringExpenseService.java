package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.RecurringExpense;
import com.financial.entity.Transaction;
import com.financial.repository.RecurringExpenseRepository;
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
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RecurringExpenseService {
    
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    
    /**
     * Get all recurring expenses with pagination.
     *
     * @param pageable pagination information
     * @return page of recurring expenses
     */
    @Transactional(readOnly = true)
    public Page<RecurringExpense> getAllRecurringExpenses(Pageable pageable) {
        return recurringExpenseRepository.findAll(pageable);
    }
    
    /**
     * Get all recurring expenses.
     *
     * @return list of all recurring expenses
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getAllRecurringExpenses() {
        return recurringExpenseRepository.findAll();
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
     * Get recurring expenses by status.
     *
     * @param status the status
     * @return list of recurring expenses with the specified status
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByStatus(RecurringExpense.Status status) {
        return recurringExpenseRepository.findByStatus(status);
    }
    
    /**
     * Get recurring expenses by frequency.
     *
     * @param frequency the frequency
     * @return list of recurring expenses with the specified frequency
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesByFrequency(RecurringExpense.Frequency frequency) {
        return recurringExpenseRepository.findByFrequency(frequency);
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
     * Search recurring expenses by name.
     *
     * @param name the name pattern to search for
     * @return list of recurring expenses matching the pattern
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> searchRecurringExpensesByName(String name) {
        return recurringExpenseRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Search recurring expenses by provider.
     *
     * @param provider the provider pattern to search for
     * @return list of recurring expenses matching the pattern
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> searchRecurringExpensesByProvider(String provider) {
        return recurringExpenseRepository.findByProviderContainingIgnoreCase(provider);
    }
    
    /**
     * Get recurring expenses due today.
     *
     * @return list of recurring expenses due today
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesDueToday() {
        return recurringExpenseRepository.findDueToday(LocalDate.now());
    }
    
    /**
     * Get overdue recurring expenses.
     *
     * @return list of overdue recurring expenses
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getOverdueRecurringExpenses() {
        return recurringExpenseRepository.findOverdue(LocalDate.now());
    }
    
    /**
     * Get recurring expenses due soon.
     *
     * @param daysAhead the number of days ahead to check
     * @return list of recurring expenses due soon
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesDueSoon(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);
        return recurringExpenseRepository.findDueSoon(today, futureDate);
    }
    
    /**
     * Get recurring expenses with auto-pay enabled.
     *
     * @return list of recurring expenses with auto-pay enabled
     */
    @Transactional(readOnly = true)
    public List<RecurringExpense> getRecurringExpensesWithAutoPay() {
        return recurringExpenseRepository.findByIsAutoPay(true);
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
     * Get recurring expense by ID.
     *
     * @param id the recurring expense ID
     * @return Optional containing the recurring expense if found
     */
    @Transactional(readOnly = true)
    public Optional<RecurringExpense> getRecurringExpenseById(Long id) {
        return recurringExpenseRepository.findById(id);
    }
    
    /**
     * Create a new recurring expense.
     *
     * @param recurringExpense the recurring expense to create
     * @return the created recurring expense
     */
    public RecurringExpense createRecurringExpense(RecurringExpense recurringExpense) {
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
     * Update an existing recurring expense.
     *
     * @param recurringExpense the recurring expense to update
     * @return the updated recurring expense
     */
    public RecurringExpense updateRecurringExpense(RecurringExpense recurringExpense) {
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Delete recurring expense by ID.
     *
     * @param id the recurring expense ID
     * @throws IllegalArgumentException if recurring expense not found
     */
    public void deleteRecurringExpense(Long id) {
        if (!recurringExpenseRepository.existsById(id)) {
            throw new IllegalArgumentException("Recurring expense with ID " + id + " not found");
        }
        
        recurringExpenseRepository.deleteById(id);
    }
    
    /**
     * Mark recurring expense as paid and create transaction.
     *
     * @param id the recurring expense ID
     * @return the updated recurring expense
     * @throws IllegalArgumentException if recurring expense not found
     */
    public RecurringExpense markAsPaid(Long id) {
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
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
                .build();
        
        // Create transaction and update account balance
        transactionService.createTransaction(transaction);
        accountService.subtractFromAccountBalance(recurringExpense.getAccount().getId(), recurringExpense.getAmount());
        
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Pause a recurring expense.
     *
     * @param id the recurring expense ID
     * @return the updated recurring expense
     * @throws IllegalArgumentException if recurring expense not found
     */
    public RecurringExpense pauseRecurringExpense(Long id) {
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + id + " not found"));
        
        recurringExpense.setStatus(RecurringExpense.Status.PAUSED);
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Resume a paused recurring expense.
     *
     * @param id the recurring expense ID
     * @return the updated recurring expense
     * @throws IllegalArgumentException if recurring expense not found
     */
    public RecurringExpense resumeRecurringExpense(Long id) {
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + id + " not found"));
        
        recurringExpense.setStatus(RecurringExpense.Status.ACTIVE);
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Cancel a recurring expense.
     *
     * @param id the recurring expense ID
     * @return the updated recurring expense
     * @throws IllegalArgumentException if recurring expense not found
     */
    public RecurringExpense cancelRecurringExpense(Long id) {
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense with ID " + id + " not found"));
        
        recurringExpense.setStatus(RecurringExpense.Status.CANCELLED);
        return recurringExpenseRepository.save(recurringExpense);
    }
    
    /**
     * Process all due recurring expenses (for scheduled job).
     *
     * @return number of processed expenses
     */
    @Transactional
    public int processAllDueRecurringExpenses() {
        List<RecurringExpense> dueExpenses = recurringExpenseRepository.findDueToday(LocalDate.now());
        
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
