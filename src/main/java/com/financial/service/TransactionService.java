package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.Transaction;
import com.financial.entity.User;
import com.financial.repository.TransactionRepository;
import com.financial.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for transaction-related business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * Get all transactions for the authenticated user with pagination.
     *
     * @param pageable pagination information
     * @return page of transactions
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return transactionRepository.findByUser(currentUser, pageable);
    }

    /**
     * Get all transactions for the authenticated user.
     *
     * @return list of all transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return transactionRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
    }

    /**
     * Get the last N transactions for the authenticated user ordered by date descending.
     *
     * @param limit the number of transactions to retrieve
     * @return list of last N transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getLastTransactions(int limit) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        Pageable pageable = PageRequest.of(0, limit);
        Page<Transaction> page = transactionRepository.findLastTransactionsByUser(currentUser, pageable);
        return page.getContent();
    }

    /**
     * Get the last 5 transactions for the authenticated user ordered by date descending.
     *
     * @return list of last 5 transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getLast5Transactions() {
        return getLastTransactions(5);
    }

    /**
     * Get the last N transactions for a specific account.
     *
     * @param account the account
     * @param limit   the number of transactions to retrieve
     * @return list of last N transactions for the account
     */
    @Transactional(readOnly = true)
    public List<Transaction> getLastTransactionsByAccount(Account account, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Transaction> page = transactionRepository.findLastTransactionsByAccount(account, pageable);
        return page.getContent();
    }

    /**
     * Get transactions by account.
     *
     * @param account the account
     * @return list of transactions for the account
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccount(Account account) {
        return transactionRepository.findByAccount(account);
    }

    /**
     * Get transactions by account with pagination.
     *
     * @param account  the account
     * @param pageable pagination information
     * @return page of transactions for the account
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByAccount(Account account, Pageable pageable) {
        return transactionRepository.findByAccount(account, pageable);
    }

    /**
     * Get transactions by category.
     *
     * @param category the category
     * @return list of transactions for the category
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByCategory(Category category) {
        return transactionRepository.findByCategory(category);
    }

    /**
     * Get transactions by type for the authenticated user.
     *
     * @param type the transaction type
     * @return list of transactions with the specified type
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return transactionRepository.findByUserAndType(currentUser, type);
    }

    /**
     * Get transactions by account and type.
     *
     * @param account the account
     * @param type    the transaction type
     * @return list of transactions for the account and type
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountAndType(Account account, Transaction.TransactionType type) {
        return transactionRepository.findByAccountAndType(account, type);
    }

    /**
     * Get transactions by account and category.
     *
     * @param account  the account
     * @param category the category
     * @return list of transactions for the account and category
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountAndCategory(Account account, Category category) {
        return transactionRepository.findByAccountAndCategory(account, category);
    }

    /**
     * Get transactions by date range for the authenticated user.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of transactions within the date range
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return transactionRepository.findByUserAndTransactionDateBetween(currentUser, startDate, endDate);
    }

    /**
     * Get transactions by account and date range.
     *
     * @param account   the account
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of transactions for the account within the date range
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountAndDateRange(Account account, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByAccountAndTransactionDateBetween(account, startDate, endDate);
    }

    /**
     * Get transactions by amount range.
     *
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of transactions within the amount range
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount);
    }

    /**
     * Get transactions by account and amount range.
     *
     * @param account   the account
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of transactions for the account within the amount range
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountAndAmountRange(Account account, BigDecimal minAmount, BigDecimal maxAmount) {
        return transactionRepository.findByAccountAndAmountBetween(account, minAmount, maxAmount);
    }

    /**
     * Search transactions by description for the authenticated user.
     *
     * @param description the description pattern to search for
     * @return list of transactions matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Transaction> searchTransactionsByDescription(String description) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return transactionRepository.findByUserAndDescriptionContainingIgnoreCase(currentUser, description);
    }

    /**
     * Search transactions by account and description.
     *
     * @param account     the account
     * @param description the description pattern to search for
     * @return list of transactions for the account matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Transaction> searchTransactionsByAccountAndDescription(Account account, String description) {
        return transactionRepository.findByAccountAndDescriptionContainingIgnoreCase(account, description);
    }

    /**
     * Get recurring transactions.
     *
     * @param isRecurring whether the transaction is recurring
     * @return list of recurring transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getRecurringTransactions(Boolean isRecurring) {
        return transactionRepository.findByIsRecurring(isRecurring);
    }

    /**
     * Get transaction by ID for the authenticated user.
     *
     * @param id the transaction ID
     * @return Optional containing the transaction if found
     */
    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionById(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return transactionRepository.findByIdAndUser(id, currentUser);
    }

    /**
     * Calculate total amount by transaction type for the authenticated user.
     *
     * @param type the transaction type
     * @return total amount for the transaction type
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAmountByType(Transaction.TransactionType type) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return transactionRepository.calculateTotalAmountByUserAndType(currentUser, type);
    }

    /**
     * Calculate total amount by account and transaction type.
     *
     * @param account the account
     * @param type    the transaction type
     * @return total amount for the account and transaction type
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAmountByAccountAndType(Account account, Transaction.TransactionType type) {
        return transactionRepository.calculateTotalAmountByAccountAndType(account, type);
    }

    /**
     * Calculate total amount by account and date range.
     *
     * @param account   the account
     * @param startDate the start date
     * @param endDate   the end date
     * @return total amount for the account within the date range
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAmountByAccountAndDateRange(Account account, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.calculateTotalAmountByAccountAndDateRange(account, startDate, endDate);
    }

    /**
     * Calculate total income amount for an account.
     *
     * @param account the account
     * @return total income amount
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalIncomeByAccount(Account account) {
        return transactionRepository.calculateTotalIncomeByAccount(account);
    }

    /**
     * Calculate total expense amount for an account.
     *
     * @param account the account
     * @return total expense amount
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalExpenseByAccount(Account account) {
        return transactionRepository.calculateTotalExpenseByAccount(account);
    }

    /**
     * Create a new transaction for the authenticated user.
     *
     * @param transaction the transaction to create
     * @return the created transaction
     */
    public Transaction createTransaction(Transaction transaction) {
        User currentUser = SecurityUtils.getAuthenticatedUser();

        // Verify account belongs to current user if account is specified
        if (transaction.getAccount() != null) {
            accountService.getAccountById(transaction.getAccount().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found or doesn't belong to you"));
        }

        // Associate transaction with current user
        transaction.setUser(currentUser);

        return transactionRepository.save(transaction);
    }

    /**
     * Update an existing transaction for the authenticated user.
     *
     * @param transaction the transaction to update
     * @return the updated transaction
     * @throws IllegalArgumentException if transaction not found or doesn't belong to user
     */
    public Transaction updateTransaction(Transaction transaction) {
        User currentUser = SecurityUtils.getAuthenticatedUser();

        // Verify transaction belongs to current user
        Transaction existingTransaction = transactionRepository.findByIdAndUser(transaction.getId(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Transaction with ID " + transaction.getId() + " not found"));

        // Verify account belongs to current user if account is specified
        if (transaction.getAccount() != null) {
            accountService.getAccountById(transaction.getAccount().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found or doesn't belong to you"));
        }

        // Ensure user association is not changed
        transaction.setUser(currentUser);

        return transactionRepository.save(transaction);
    }

    /**
     * Delete transaction by ID for the authenticated user.
     *
     * @param id the transaction ID
     * @throws IllegalArgumentException if transaction not found or doesn't belong to user
     */
    public void deleteTransaction(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();

        Transaction transaction = transactionRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Transaction with ID " + id + " not found"));

        transactionRepository.delete(transaction);
    }

    /**
     * Get transactions by reference number for the authenticated user.
     *
     * @param referenceNumber the reference number
     * @return list of transactions with the reference number
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByReferenceNumber(String referenceNumber) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Transaction> allTransactions = transactionRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allTransactions.stream()
                .filter(t -> t.getReferenceNumber() != null &&
                        t.getReferenceNumber().equals(referenceNumber))
                .toList();
    }

    /**
     * Search transactions by location for the authenticated user.
     *
     * @param location the location pattern to search for
     * @return list of transactions matching the location pattern
     */
    @Transactional(readOnly = true)
    public List<Transaction> searchTransactionsByLocation(String location) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Transaction> allTransactions = transactionRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allTransactions.stream()
                .filter(t -> t.getLocation() != null &&
                        t.getLocation().toLowerCase().contains(location.toLowerCase()))
                .toList();
    }
}
