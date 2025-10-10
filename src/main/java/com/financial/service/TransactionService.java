package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.Transaction;
import com.financial.repository.TransactionRepository;
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
    
    /**
     * Get all transactions with pagination.
     *
     * @param pageable pagination information
     * @return page of transactions
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }
    
    /**
     * Get all transactions.
     *
     * @return list of all transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    /**
     * Get the last N transactions ordered by date descending.
     *
     * @param limit the number of transactions to retrieve
     * @return list of last N transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getLastTransactions(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Transaction> page = transactionRepository.findLastTransactions(pageable);
        return page.getContent();
    }
    
    /**
     * Get the last 5 transactions ordered by date descending.
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
     * @param limit the number of transactions to retrieve
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
     * @param account the account
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
     * Get transactions by type.
     *
     * @param type the transaction type
     * @return list of transactions with the specified type
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) {
        return transactionRepository.findByType(type);
    }
    
    /**
     * Get transactions by account and type.
     *
     * @param account the account
     * @param type the transaction type
     * @return list of transactions for the account and type
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountAndType(Account account, Transaction.TransactionType type) {
        return transactionRepository.findByAccountAndType(account, type);
    }
    
    /**
     * Get transactions by account and category.
     *
     * @param account the account
     * @param category the category
     * @return list of transactions for the account and category
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountAndCategory(Account account, Category category) {
        return transactionRepository.findByAccountAndCategory(account, category);
    }
    
    /**
     * Get transactions by date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of transactions within the date range
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }
    
    /**
     * Get transactions by account and date range.
     *
     * @param account the account
     * @param startDate the start date
     * @param endDate the end date
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
     * @param account the account
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of transactions for the account within the amount range
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountAndAmountRange(Account account, BigDecimal minAmount, BigDecimal maxAmount) {
        return transactionRepository.findByAccountAndAmountBetween(account, minAmount, maxAmount);
    }
    
    /**
     * Search transactions by description.
     *
     * @param description the description pattern to search for
     * @return list of transactions matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Transaction> searchTransactionsByDescription(String description) {
        return transactionRepository.findByDescriptionContainingIgnoreCase(description);
    }
    
    /**
     * Search transactions by account and description.
     *
     * @param account the account
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
     * Get transaction by ID.
     *
     * @param id the transaction ID
     * @return Optional containing the transaction if found
     */
    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    /**
     * Calculate total amount by transaction type.
     *
     * @param type the transaction type
     * @return total amount for the transaction type
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAmountByType(Transaction.TransactionType type) {
        return transactionRepository.calculateTotalAmountByType(type);
    }
    
    /**
     * Calculate total amount by account and transaction type.
     *
     * @param account the account
     * @param type the transaction type
     * @return total amount for the account and transaction type
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAmountByAccountAndType(Account account, Transaction.TransactionType type) {
        return transactionRepository.calculateTotalAmountByAccountAndType(account, type);
    }
    
    /**
     * Calculate total amount by account and date range.
     *
     * @param account the account
     * @param startDate the start date
     * @param endDate the end date
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
     * Create a new transaction.
     *
     * @param transaction the transaction to create
     * @return the created transaction
     */
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    /**
     * Update an existing transaction.
     *
     * @param transaction the transaction to update
     * @return the updated transaction
     */
    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    /**
     * Delete transaction by ID.
     *
     * @param id the transaction ID
     * @throws IllegalArgumentException if transaction not found
     */
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transaction with ID " + id + " not found");
        }
        
        transactionRepository.deleteById(id);
    }
    
    /**
     * Get transactions by reference number.
     *
     * @param referenceNumber the reference number
     * @return list of transactions with the reference number
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByReferenceNumber(String referenceNumber) {
        return transactionRepository.findByReferenceNumber(referenceNumber);
    }
    
    /**
     * Search transactions by location.
     *
     * @param location the location pattern to search for
     * @return list of transactions matching the location pattern
     */
    @Transactional(readOnly = true)
    public List<Transaction> searchTransactionsByLocation(String location) {
        return transactionRepository.findByLocationContainingIgnoreCase(location);
    }
}
