package com.financial.repository;

import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.Transaction;
import com.financial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transaction by ID and user.
     *
     * @param id the transaction ID
     * @param user the user
     * @return Optional containing the transaction if found
     */
    Optional<Transaction> findByIdAndUser(Long id, User user);
    
    /**
     * Find all transactions by user with pagination.
     *
     * @param user the user
     * @param pageable pagination information
     * @return page of user's transactions
     */
    Page<Transaction> findByUser(User user, Pageable pageable);
    
    /**
     * Find transactions by user and type.
     *
     * @param user the user
     * @param type the transaction type
     * @return list of transactions for the user with the specified type
     */
    List<Transaction> findByUserAndType(User user, Transaction.TransactionType type);
    
    /**
     * Find transactions by user and date range.
     *
     * @param user the user
     * @param startDate the start date
     * @param endDate the end date
     * @return list of transactions for the user within the date range
     */
    List<Transaction> findByUserAndTransactionDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find transactions by user, ordered by transaction date descending.
     *
     * @param user the user
     * @param pageable pagination information
     * @return page of transactions ordered by date descending
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user ORDER BY t.transactionDate DESC")
    Page<Transaction> findLastTransactionsByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * Calculate total amount by user and transaction type.
     *
     * @param user the user
     * @param type the transaction type
     * @return total amount for the user and transaction type
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    BigDecimal calculateTotalAmountByUserAndType(@Param("user") User user, @Param("type") Transaction.TransactionType type);
    
    /**
     * Calculate total amount by user and date range.
     *
     * @param user the user
     * @param startDate the start date
     * @param endDate the end date
     * @return total amount for the user within the date range
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalAmountByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate total income amount for a user.
     *
     * @param user the user
     * @return total income amount
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = 'INCOME'")
    BigDecimal calculateTotalIncomeByUser(@Param("user") User user);
    
    /**
     * Calculate total expense amount for a user.
     *
     * @param user the user
     * @return total expense amount
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE'")
    BigDecimal calculateTotalExpenseByUser(@Param("user") User user);
    
    /**
     * Find transactions by user and description containing (case-insensitive).
     *
     * @param user the user
     * @param description the description pattern to search for
     * @return list of transactions matching the pattern
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Transaction> findByUserAndDescriptionContainingIgnoreCase(@Param("user") User user, @Param("description") String description);
    
    /**
     * Find transactions by account.
     *
     * @param account the account
     * @return list of transactions for the account
     */
    List<Transaction> findByAccount(Account account);
    
    /**
     * Find transactions by account with pagination.
     *
     * @param account the account
     * @param pageable pagination information
     * @return page of transactions for the account
     */
    Page<Transaction> findByAccount(Account account, Pageable pageable);
    
    /**
     * Find transactions by category.
     *
     * @param category the category
     * @return list of transactions for the category
     */
    List<Transaction> findByCategory(Category category);
    
    /**
     * Find transactions by type.
     *
     * @param type the transaction type
     * @return list of transactions with the specified type
     */
    List<Transaction> findByType(Transaction.TransactionType type);
    
    /**
     * Find transactions by account and type.
     *
     * @param account the account
     * @param type the transaction type
     * @return list of transactions for the account and type
     */
    List<Transaction> findByAccountAndType(Account account, Transaction.TransactionType type);
    
    /**
     * Find transactions by account and category.
     *
     * @param account the account
     * @param category the category
     * @return list of transactions for the account and category
     */
    List<Transaction> findByAccountAndCategory(Account account, Category category);
    
    /**
     * Find transactions by date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of transactions within the date range
     */
    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find transactions by account and date range.
     *
     * @param account the account
     * @param startDate the start date
     * @param endDate the end date
     * @return list of transactions for the account within the date range
     */
    List<Transaction> findByAccountAndTransactionDateBetween(Account account, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find transactions by amount range.
     *
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of transactions within the amount range
     */
    List<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Find transactions by account and amount range.
     *
     * @param account the account
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of transactions for the account within the amount range
     */
    List<Transaction> findByAccountAndAmountBetween(Account account, BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Find transactions by description containing (case-insensitive).
     *
     * @param description the description pattern to search for
     * @return list of transactions matching the pattern
     */
    @Query("SELECT t FROM Transaction t WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Transaction> findByDescriptionContainingIgnoreCase(@Param("description") String description);
    
    /**
     * Find transactions by account and description containing (case-insensitive).
     *
     * @param account the account
     * @param description the description pattern to search for
     * @return list of transactions for the account matching the pattern
     */
    @Query("SELECT t FROM Transaction t WHERE t.account = :account AND LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Transaction> findByAccountAndDescriptionContainingIgnoreCase(@Param("account") Account account, @Param("description") String description);
    
    /**
     * Find recurring transactions.
     *
     * @param isRecurring whether the transaction is recurring
     * @return list of recurring transactions
     */
    List<Transaction> findByIsRecurring(Boolean isRecurring);
    
    /**
     * Find the last N transactions ordered by transaction date descending.
     *
     * @param pageable pagination information
     * @return page of transactions ordered by date descending
     */
    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC")
    Page<Transaction> findLastTransactions(Pageable pageable);
    
    /**
     * Find the last N transactions for a specific account ordered by transaction date descending.
     *
     * @param account the account
     * @param pageable pagination information
     * @return page of transactions for the account ordered by date descending
     */
    @Query("SELECT t FROM Transaction t WHERE t.account = :account ORDER BY t.transactionDate DESC")
    Page<Transaction> findLastTransactionsByAccount(@Param("account") Account account, Pageable pageable);
    
    /**
     * Calculate total amount by transaction type.
     *
     * @param type the transaction type
     * @return total amount for the transaction type
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal calculateTotalAmountByType(@Param("type") Transaction.TransactionType type);
    
    /**
     * Calculate total amount by account and transaction type.
     *
     * @param account the account
     * @param type the transaction type
     * @return total amount for the account and transaction type
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account = :account AND t.type = :type")
    BigDecimal calculateTotalAmountByAccountAndType(@Param("account") Account account, @Param("type") Transaction.TransactionType type);
    
    /**
     * Calculate total amount by account and date range.
     *
     * @param account the account
     * @param startDate the start date
     * @param endDate the end date
     * @return total amount for the account within the date range
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account = :account AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalAmountByAccountAndDateRange(@Param("account") Account account, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate total income amount for an account.
     *
     * @param account the account
     * @return total income amount
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account = :account AND t.type = 'INCOME'")
    BigDecimal calculateTotalIncomeByAccount(@Param("account") Account account);
    
    /**
     * Calculate total expense amount for an account.
     *
     * @param account the account
     * @return total expense amount
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account = :account AND t.type = 'EXPENSE'")
    BigDecimal calculateTotalExpenseByAccount(@Param("account") Account account);
    
    /**
     * Find transactions by reference number.
     *
     * @param referenceNumber the reference number
     * @return list of transactions with the reference number
     */
    List<Transaction> findByReferenceNumber(String referenceNumber);
    
    /**
     * Find transactions by location containing (case-insensitive).
     *
     * @param location the location pattern to search for
     * @return list of transactions matching the location pattern
     */
    @Query("SELECT t FROM Transaction t WHERE LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Transaction> findByLocationContainingIgnoreCase(@Param("location") String location);
}
