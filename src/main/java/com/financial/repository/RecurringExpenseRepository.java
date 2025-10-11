package com.financial.repository;

import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.RecurringExpense;
import com.financial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RecurringExpense entity operations.
 * Optimized to avoid N+1 query problems using @EntityGraph and fetch joins.
 */
@Repository
public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    
    // User-based queries
    /**
     * Find recurring expense by ID and user.
     * Uses EntityGraph to eagerly load account and category to avoid N+1 queries.
     *
     * @param id the recurring expense ID
     * @param user the user
     * @return Optional containing the recurring expense if found
     */
    @EntityGraph(attributePaths = {"account", "category"})
    Optional<RecurringExpense> findByIdAndUser(Long id, User user);
    
    /**
     * Find all recurring expenses by user with pagination.
     * Uses EntityGraph to eagerly load account and category to avoid N+1 queries.
     *
     * @param user the user
     * @param pageable pagination information
     * @return page of user's recurring expenses
     */
    @EntityGraph(attributePaths = {"account", "category"})
    Page<RecurringExpense> findByUser(User user, Pageable pageable);
    
    /**
     * Find recurring expenses by user and status.
     * Uses EntityGraph to eagerly load account and category to avoid N+1 queries.
     *
     * @param user the user
     * @param status the status
     * @return list of recurring expenses
     */
    @EntityGraph(attributePaths = {"account", "category"})
    List<RecurringExpense> findByUserAndStatus(User user, RecurringExpense.Status status);
    
    /**
     * Find recurring expenses due today for a user.
     * Uses fetch join to eagerly load account and category to avoid N+1 queries.
     *
     * @param user the user
     * @param today the current date
     * @return list of recurring expenses due today
     */
    @Query("SELECT DISTINCT r FROM RecurringExpense r " +
           "LEFT JOIN FETCH r.account " +
           "LEFT JOIN FETCH r.category " +
           "WHERE r.user = :user AND r.nextDueDate = :today AND r.status = 'ACTIVE'")
    List<RecurringExpense> findDueTodayByUser(@Param("user") User user, @Param("today") LocalDate today);
    
    /**
     * Find overdue recurring expenses for a user.
     * Uses fetch join to eagerly load account and category to avoid N+1 queries.
     *
     * @param user the user
     * @param today the current date
     * @return list of overdue recurring expenses
     */
    @Query("SELECT DISTINCT r FROM RecurringExpense r " +
           "LEFT JOIN FETCH r.account " +
           "LEFT JOIN FETCH r.category " +
           "WHERE r.user = :user AND r.nextDueDate < :today AND r.status = 'ACTIVE'")
    List<RecurringExpense> findOverdueByUser(@Param("user") User user, @Param("today") LocalDate today);
    
    /**
     * Calculate total monthly recurring expenses for a user.
     *
     * @param user the user
     * @return total monthly recurring expenses
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RecurringExpense r WHERE r.user = :user AND r.status = 'ACTIVE' AND r.frequency = 'MONTHLY'")
    BigDecimal calculateTotalMonthlyRecurringExpensesByUser(@Param("user") User user);
    
    /**
     * Find recurring expenses by account.
     * Uses EntityGraph to eagerly load category to avoid N+1 queries.
     *
     * @param account the account
     * @return list of recurring expenses for the account
     */
    @EntityGraph(attributePaths = {"category"})
    List<RecurringExpense> findByAccount(Account account);
    
    /**
     * Find recurring expenses by account with pagination.
     * Uses EntityGraph to eagerly load category to avoid N+1 queries.
     *
     * @param account the account
     * @param pageable pagination information
     * @return page of recurring expenses for the account
     */
    @EntityGraph(attributePaths = {"category"})
    Page<RecurringExpense> findByAccount(Account account, Pageable pageable);
    
    /**
     * Find recurring expenses by category.
     *
     * @param category the category
     * @return list of recurring expenses for the category
     */
    List<RecurringExpense> findByCategory(Category category);
    
    /**
     * Find recurring expenses by status.
     *
     * @param status the status
     * @return list of recurring expenses with the specified status
     */
    List<RecurringExpense> findByStatus(RecurringExpense.Status status);
    
    /**
     * Find recurring expenses by frequency.
     *
     * @param frequency the frequency
     * @return list of recurring expenses with the specified frequency
     */
    List<RecurringExpense> findByFrequency(RecurringExpense.Frequency frequency);
    
    /**
     * Find recurring expenses by account and status.
     *
     * @param account the account
     * @param status the status
     * @return list of recurring expenses for the account and status
     */
    List<RecurringExpense> findByAccountAndStatus(Account account, RecurringExpense.Status status);
    
    /**
     * Find recurring expenses by provider.
     *
     * @param provider the provider name
     * @return list of recurring expenses for the provider
     */
    List<RecurringExpense> findByProvider(String provider);
    
    /**
     * Find recurring expenses by provider containing (case-insensitive).
     *
     * @param provider the provider pattern to search for
     * @return list of recurring expenses matching the pattern
     */
    @Query("SELECT r FROM RecurringExpense r WHERE LOWER(r.provider) LIKE LOWER(CONCAT('%', :provider, '%'))")
    List<RecurringExpense> findByProviderContainingIgnoreCase(@Param("provider") String provider);
    
    /**
     * Find recurring expenses by name containing (case-insensitive).
     *
     * @param name the name pattern to search for
     * @return list of recurring expenses matching the pattern
     */
    @Query("SELECT r FROM RecurringExpense r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RecurringExpense> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find recurring expenses due today.
     *
     * @param today the current date
     * @return list of recurring expenses due today
     */
    @Query("SELECT r FROM RecurringExpense r WHERE r.nextDueDate = :today AND r.status = 'ACTIVE'")
    List<RecurringExpense> findDueToday(@Param("today") LocalDate today);
    
    /**
     * Find overdue recurring expenses.
     *
     * @param today the current date
     * @return list of overdue recurring expenses
     */
    @Query("SELECT r FROM RecurringExpense r WHERE r.nextDueDate < :today AND r.status = 'ACTIVE'")
    List<RecurringExpense> findOverdue(@Param("today") LocalDate today);
    
    /**
     * Find recurring expenses due soon (within specified days).
     *
     * @param today the current date
     * @param daysAhead the number of days ahead to check
     * @return list of recurring expenses due soon
     */
    @Query("SELECT r FROM RecurringExpense r WHERE r.nextDueDate BETWEEN :today AND :futureDate AND r.status = 'ACTIVE'")
    List<RecurringExpense> findDueSoon(@Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);
    
    /**
     * Find recurring expenses with auto-pay enabled.
     *
     * @param isAutoPay whether auto-pay is enabled
     * @return list of recurring expenses with auto-pay enabled
     */
    List<RecurringExpense> findByIsAutoPay(Boolean isAutoPay);
    
    /**
     * Find recurring expenses by amount range.
     *
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of recurring expenses within the amount range
     */
    List<RecurringExpense> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Find recurring expenses by account and amount range.
     *
     * @param account the account
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of recurring expenses for the account within the amount range
     */
    List<RecurringExpense> findByAccountAndAmountBetween(Account account, BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Calculate total monthly recurring expenses for an account.
     *
     * @param account the account
     * @return total monthly recurring expenses
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RecurringExpense r WHERE r.account = :account AND r.status = 'ACTIVE' AND r.frequency = 'MONTHLY'")
    BigDecimal calculateTotalMonthlyRecurringExpenses(@Param("account") Account account);
    
    /**
     * Calculate total recurring expenses by frequency for an account.
     *
     * @param account the account
     * @param frequency the frequency
     * @return total recurring expenses for the frequency
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RecurringExpense r WHERE r.account = :account AND r.status = 'ACTIVE' AND r.frequency = :frequency")
    BigDecimal calculateTotalRecurringExpensesByFrequency(@Param("account") Account account, @Param("frequency") RecurringExpense.Frequency frequency);
    
    /**
     * Calculate total recurring expenses for an account.
     *
     * @param account the account
     * @return total recurring expenses
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RecurringExpense r WHERE r.account = :account AND r.status = 'ACTIVE'")
    BigDecimal calculateTotalRecurringExpenses(@Param("account") Account account);
    
    /**
     * Find recurring expenses by reference number.
     *
     * @param referenceNumber the reference number
     * @return list of recurring expenses with the reference number
     */
    List<RecurringExpense> findByReferenceNumber(String referenceNumber);
    
    /**
     * Find all recurring expenses with pagination.
     *
     * @param pageable pagination information
     * @return page of recurring expenses
     */
    Page<RecurringExpense> findAll(Pageable pageable);
    
    /**
     * Find recurring expenses by status with pagination.
     *
     * @param status the status
     * @param pageable pagination information
     * @return page of recurring expenses with the specified status
     */
    Page<RecurringExpense> findByStatus(RecurringExpense.Status status, Pageable pageable);
    
    /**
     * Find recurring expenses by frequency with pagination.
     *
     * @param frequency the frequency
     * @param pageable pagination information
     * @return page of recurring expenses with the specified frequency
     */
    Page<RecurringExpense> findByFrequency(RecurringExpense.Frequency frequency, Pageable pageable);
}
