package com.financial.repository;

import com.financial.entity.Account;
import com.financial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity operations.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Find account by name and user.
     *
     * @param name the account name to search for
     * @param user the user
     * @return Optional containing the account if found
     */
    Optional<Account> findByNameAndUser(String name, User user);
    
    /**
     * Find account by name (without user filtering - for backward compatibility).
     *
     * @param name the account name to search for
     * @return Optional containing the account if found
     */
    Optional<Account> findByName(String name);
    
    /**
     * Find accounts by user and type.
     *
     * @param user the user
     * @param type the account type
     * @return list of accounts with the specified type
     */
    List<Account> findByUserAndType(User user, Account.AccountType type);
    
    /**
     * Find accounts by user and status.
     *
     * @param user the user
     * @param status the account status
     * @return list of accounts with the specified status
     */
    List<Account> findByUserAndStatus(User user, Account.AccountStatus status);
    
    /**
     * Find accounts by user, type and status.
     *
     * @param user the user
     * @param type the account type
     * @param status the account status
     * @return list of accounts with the specified type and status
     */
    List<Account> findByUserAndTypeAndStatus(User user, Account.AccountType type, Account.AccountStatus status);
    
    /**
     * Find accounts by user that should be included in balance calculation.
     *
     * @param user the user
     * @param includeInBalance whether to include in balance
     * @param status the account status
     * @return list of accounts included in balance
     */
    List<Account> findByUserAndIncludeInBalanceAndStatus(User user, Boolean includeInBalance, Account.AccountStatus status);
    
    /**
     * Find all user's accounts with pagination.
     *
     * @param user the user
     * @param pageable pagination information
     * @return page of user's accounts
     */
    Page<Account> findByUser(User user, Pageable pageable);
    
    /**
     * Find all user's active accounts with pagination.
     *
     * @param user the user
     * @param status the account status
     * @param pageable pagination information
     * @return page of active accounts
     */
    Page<Account> findByUserAndStatus(User user, Account.AccountStatus status, Pageable pageable);
    
    /**
     * Find accounts by type.
     *
     * @param type the account type
     * @return list of accounts with the specified type
     */
    List<Account> findByType(Account.AccountType type);
    
    /**
     * Find accounts by status.
     *
     * @param status the account status
     * @return list of accounts with the specified status
     */
    List<Account> findByStatus(Account.AccountStatus status);
    
    /**
     * Find accounts by type and status.
     *
     * @param type the account type
     * @param status the account status
     * @return list of accounts with the specified type and status
     */
    List<Account> findByTypeAndStatus(Account.AccountType type, Account.AccountStatus status);
    
    /**
     * Find accounts that should be included in balance calculation.
     *
     * @param includeInBalance whether to include in balance
     * @param status the account status
     * @return list of accounts included in balance
     */
    List<Account> findByIncludeInBalanceAndStatus(Boolean includeInBalance, Account.AccountStatus status);
    
    /**
     * Find all active accounts with pagination.
     *
     * @param status the account status
     * @param pageable pagination information
     * @return page of active accounts
     */
    Page<Account> findByStatus(Account.AccountStatus status, Pageable pageable);
    
    /**
     * Check if account exists by name and user.
     *
     * @param name the account name to check
     * @param user the user
     * @return true if account exists, false otherwise
     */
    boolean existsByNameAndUser(String name, User user);
    
    /**
     * Check if account exists by name and user excluding a specific ID.
     *
     * @param name the account name to check
     * @param user the user
     * @param id the ID to exclude
     * @return true if account exists, false otherwise
     */
    boolean existsByNameAndUserAndIdNot(String name, User user, Long id);
    
    /**
     * Find account by ID and user.
     *
     * @param id the account ID
     * @param user the user
     * @return Optional containing the account if found
     */
    Optional<Account> findByIdAndUser(Long id, User user);
    
    /**
     * Check if account exists by name.
     *
     * @param name the account name to check
     * @return true if account exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Check if account exists by name excluding a specific ID.
     *
     * @param name the account name to check
     * @param id the ID to exclude
     * @return true if account exists, false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * Find user's accounts by name containing (case-insensitive).
     *
     * @param user the user
     * @param name the name pattern to search for
     * @return list of accounts matching the pattern
     */
    @Query("SELECT a FROM Account a WHERE a.user = :user AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Account> findByUserAndNameContainingIgnoreCase(@Param("user") User user, @Param("name") String name);
    
    /**
     * Find user's accounts by name containing and status (case-insensitive).
     *
     * @param user the user
     * @param name the name pattern to search for
     * @param status the account status
     * @return list of accounts matching the pattern and status
     */
    @Query("SELECT a FROM Account a WHERE a.user = :user AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) AND a.status = :status")
    List<Account> findByUserAndNameContainingIgnoreCaseAndStatus(@Param("user") User user, @Param("name") String name, @Param("status") Account.AccountStatus status);
    
    /**
     * Find accounts by name containing (case-insensitive).
     *
     * @param name the name pattern to search for
     * @return list of accounts matching the pattern
     */
    @Query("SELECT a FROM Account a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Account> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find accounts by name containing and status (case-insensitive).
     *
     * @param name the name pattern to search for
     * @param status the account status
     * @return list of accounts matching the pattern and status
     */
    @Query("SELECT a FROM Account a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) AND a.status = :status")
    List<Account> findByNameContainingIgnoreCaseAndStatus(@Param("name") String name, @Param("status") Account.AccountStatus status);
    
    /**
     * Calculate total balance for user's accounts included in balance calculation.
     *
     * @param user the user
     * @param includeInBalance whether to include in balance
     * @param status the account status
     * @return total balance
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user = :user AND a.includeInBalance = :includeInBalance AND a.status = :status")
    BigDecimal calculateTotalBalanceByUser(@Param("user") User user, @Param("includeInBalance") Boolean includeInBalance, @Param("status") Account.AccountStatus status);
    
    /**
     * Calculate total balance by user and account type.
     *
     * @param user the user
     * @param type the account type
     * @param status the account status
     * @return total balance for the account type
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user = :user AND a.type = :type AND a.status = :status")
    BigDecimal calculateTotalBalanceByUserAndType(@Param("user") User user, @Param("type") Account.AccountType type, @Param("status") Account.AccountStatus status);
    
    /**
     * Calculate total balance for accounts included in balance calculation.
     *
     * @param includeInBalance whether to include in balance
     * @param status the account status
     * @return total balance
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.includeInBalance = :includeInBalance AND a.status = :status")
    BigDecimal calculateTotalBalance(@Param("includeInBalance") Boolean includeInBalance, @Param("status") Account.AccountStatus status);
    
    /**
     * Calculate total balance by account type.
     *
     * @param type the account type
     * @param status the account status
     * @return total balance for the account type
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.type = :type AND a.status = :status")
    BigDecimal calculateTotalBalanceByType(@Param("type") Account.AccountType type, @Param("status") Account.AccountStatus status);
}
