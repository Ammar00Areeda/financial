package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.User;
import com.financial.repository.AccountRepository;
import com.financial.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for account-related business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    /**
     * Get all accounts for the authenticated user with pagination.
     *
     * @param pageable pagination information
     * @return page of accounts
     */
    @Transactional(readOnly = true)
    public Page<Account> getAllAccounts(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUser(currentUser, pageable);
    }
    
    /**
     * Get all accounts for the authenticated user.
     *
     * @return list of all accounts
     */
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndStatus(currentUser, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Get all active accounts for the authenticated user.
     *
     * @return list of active accounts
     */
    @Transactional(readOnly = true)
    public List<Account> getAllActiveAccounts() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndStatus(currentUser, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Get all active accounts for the authenticated user with pagination.
     *
     * @param pageable pagination information
     * @return page of active accounts
     */
    @Transactional(readOnly = true)
    public Page<Account> getAllActiveAccounts(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndStatus(currentUser, Account.AccountStatus.ACTIVE, pageable);
    }
    
    /**
     * Get accounts by type for the authenticated user.
     *
     * @param type the account type
     * @return list of accounts with the specified type
     */
    @Transactional(readOnly = true)
    public List<Account> getAccountsByType(Account.AccountType type) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndType(currentUser, type);
    }
    
    /**
     * Get active accounts by type for the authenticated user.
     *
     * @param type the account type
     * @return list of active accounts with the specified type
     */
    @Transactional(readOnly = true)
    public List<Account> getActiveAccountsByType(Account.AccountType type) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndTypeAndStatus(currentUser, type, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Get accounts by status for the authenticated user.
     *
     * @param status the account status
     * @return list of accounts with the specified status
     */
    @Transactional(readOnly = true)
    public List<Account> getAccountsByStatus(Account.AccountStatus status) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndStatus(currentUser, status);
    }
    
    /**
     * Get accounts included in balance calculation for the authenticated user.
     *
     * @return list of accounts included in balance
     */
    @Transactional(readOnly = true)
    public List<Account> getAccountsIncludedInBalance() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndIncludeInBalanceAndStatus(currentUser, true, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Get account by ID for the authenticated user.
     *
     * @param id the account ID
     * @return Optional containing the account if found
     */
    @Transactional(readOnly = true)
    public Optional<Account> getAccountById(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByIdAndUser(id, currentUser);
    }
    
    /**
     * Get account by name for the authenticated user.
     *
     * @param name the account name
     * @return Optional containing the account if found
     */
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByName(String name) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByNameAndUser(name, currentUser);
    }
    
    /**
     * Search accounts by name for the authenticated user.
     *
     * @param name the name pattern to search for
     * @return list of accounts matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Account> searchAccountsByName(String name) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndNameContainingIgnoreCase(currentUser, name);
    }
    
    /**
     * Search active accounts by name for the authenticated user.
     *
     * @param name the name pattern to search for
     * @return list of active accounts matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Account> searchActiveAccountsByName(String name) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.findByUserAndNameContainingIgnoreCaseAndStatus(currentUser, name, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Calculate total balance for all accounts included in balance calculation for the authenticated user.
     *
     * @return total balance
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalBalance() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.calculateTotalBalanceByUser(currentUser, true, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Calculate total balance by account type for the authenticated user.
     *
     * @param type the account type
     * @return total balance for the account type
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalBalanceByType(Account.AccountType type) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.calculateTotalBalanceByUserAndType(currentUser, type, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Create a new account for the authenticated user.
     *
     * @param account the account to create
     * @return the created account
     * @throws IllegalArgumentException if account with same name already exists
     */
    public Account createAccount(Account account) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        if (accountRepository.existsByNameAndUser(account.getName(), currentUser)) {
            throw new IllegalArgumentException("Account with name '" + account.getName() + "' already exists");
        }
        
        // Associate account with current user
        account.setUser(currentUser);
        
        if (account.getStatus() == null) {
            account.setStatus(Account.AccountStatus.ACTIVE);
        }
        
        if (account.getIncludeInBalance() == null) {
            account.setIncludeInBalance(true);
        }
        
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        
        return accountRepository.save(account);
    }
    
    /**
     * Update an existing account for the authenticated user.
     *
     * @param account the account to update
     * @return the updated account
     * @throws IllegalArgumentException if account with same name already exists (excluding current) or account not found
     */
    public Account updateAccount(Account account) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        // Verify account belongs to current user
        Account existingAccount = accountRepository.findByIdAndUser(account.getId(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + account.getId() + " not found"));
        
        if (accountRepository.existsByNameAndUserAndIdNot(account.getName(), currentUser, account.getId())) {
            throw new IllegalArgumentException("Account with name '" + account.getName() + "' already exists");
        }
        
        // Ensure user association is not changed
        account.setUser(currentUser);
        
        return accountRepository.save(account);
    }
    
    /**
     * Delete account by ID for the authenticated user.
     *
     * @param id the account ID
     * @throws IllegalArgumentException if account not found or doesn't belong to user
     */
    public void deleteAccount(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Account account = accountRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + id + " not found"));
        
        accountRepository.delete(account);
    }
    
    /**
     * Update account balance for the authenticated user.
     *
     * @param id the account ID
     * @param newBalance the new balance
     * @return the updated account
     * @throws IllegalArgumentException if account not found or doesn't belong to user
     */
    public Account updateAccountBalance(Long id, BigDecimal newBalance) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Account account = accountRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + id + " not found"));
        
        account.setBalance(newBalance);
        return accountRepository.save(account);
    }
    
    /**
     * Add amount to account balance for the authenticated user.
     *
     * @param id the account ID
     * @param amount the amount to add
     * @return the updated account
     * @throws IllegalArgumentException if account not found or doesn't belong to user
     */
    public Account addToAccountBalance(Long id, BigDecimal amount) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Account account = accountRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + id + " not found"));
        
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }
    
    /**
     * Subtract amount from account balance for the authenticated user.
     *
     * @param id the account ID
     * @param amount the amount to subtract
     * @return the updated account
     * @throws IllegalArgumentException if account not found or doesn't belong to user
     */
    public Account subtractFromAccountBalance(Long id, BigDecimal amount) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Account account = accountRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + id + " not found"));
        
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }
    
    /**
     * Check if account exists by name for the authenticated user.
     *
     * @param name the account name to check
     * @return true if account exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean accountExistsByName(String name) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.existsByNameAndUser(name, currentUser);
    }
    
    /**
     * Check if account exists by name for the authenticated user excluding a specific ID.
     *
     * @param name the account name to check
     * @param id the ID to exclude
     * @return true if account exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean accountExistsByNameAndIdNot(String name, Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return accountRepository.existsByNameAndUserAndIdNot(name, currentUser, id);
    }
}
