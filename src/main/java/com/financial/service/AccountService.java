package com.financial.service;

import com.financial.entity.Account;
import com.financial.repository.AccountRepository;
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
     * Get all accounts with pagination.
     *
     * @param pageable pagination information
     * @return page of accounts
     */
    @Transactional(readOnly = true)
    public Page<Account> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }
    
    /**
     * Get all accounts.
     *
     * @return list of all accounts
     */
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    /**
     * Get all active accounts.
     *
     * @return list of active accounts
     */
    @Transactional(readOnly = true)
    public List<Account> getAllActiveAccounts() {
        return accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Get all active accounts with pagination.
     *
     * @param pageable pagination information
     * @return page of active accounts
     */
    @Transactional(readOnly = true)
    public Page<Account> getAllActiveAccounts(Pageable pageable) {
        return accountRepository.findByStatus(Account.AccountStatus.ACTIVE, pageable);
    }
    
    /**
     * Get accounts by type.
     *
     * @param type the account type
     * @return list of accounts with the specified type
     */
    @Transactional(readOnly = true)
    public List<Account> getAccountsByType(Account.AccountType type) {
        return accountRepository.findByType(type);
    }
    
    /**
     * Get active accounts by type.
     *
     * @param type the account type
     * @return list of active accounts with the specified type
     */
    @Transactional(readOnly = true)
    public List<Account> getActiveAccountsByType(Account.AccountType type) {
        return accountRepository.findByTypeAndStatus(type, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Get accounts by status.
     *
     * @param status the account status
     * @return list of accounts with the specified status
     */
    @Transactional(readOnly = true)
    public List<Account> getAccountsByStatus(Account.AccountStatus status) {
        return accountRepository.findByStatus(status);
    }
    
    /**
     * Get accounts included in balance calculation.
     *
     * @return list of accounts included in balance
     */
    @Transactional(readOnly = true)
    public List<Account> getAccountsIncludedInBalance() {
        return accountRepository.findByIncludeInBalanceAndStatus(true, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Get account by ID.
     *
     * @param id the account ID
     * @return Optional containing the account if found
     */
    @Transactional(readOnly = true)
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    
    /**
     * Get account by name.
     *
     * @param name the account name
     * @return Optional containing the account if found
     */
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByName(String name) {
        return accountRepository.findByName(name);
    }
    
    /**
     * Search accounts by name.
     *
     * @param name the name pattern to search for
     * @return list of accounts matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Account> searchAccountsByName(String name) {
        return accountRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Search active accounts by name.
     *
     * @param name the name pattern to search for
     * @return list of active accounts matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Account> searchActiveAccountsByName(String name) {
        return accountRepository.findByNameContainingIgnoreCaseAndStatus(name, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Calculate total balance for all accounts included in balance calculation.
     *
     * @return total balance
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalBalance() {
        return accountRepository.calculateTotalBalance(true, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Calculate total balance by account type.
     *
     * @param type the account type
     * @return total balance for the account type
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalBalanceByType(Account.AccountType type) {
        return accountRepository.calculateTotalBalanceByType(type, Account.AccountStatus.ACTIVE);
    }
    
    /**
     * Create a new account.
     *
     * @param account the account to create
     * @return the created account
     * @throws IllegalArgumentException if account with same name already exists
     */
    public Account createAccount(Account account) {
        if (accountRepository.existsByName(account.getName())) {
            throw new IllegalArgumentException("Account with name '" + account.getName() + "' already exists");
        }
        
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
     * Update an existing account.
     *
     * @param account the account to update
     * @return the updated account
     * @throws IllegalArgumentException if account with same name already exists (excluding current)
     */
    public Account updateAccount(Account account) {
        if (accountRepository.existsByNameAndIdNot(account.getName(), account.getId())) {
            throw new IllegalArgumentException("Account with name '" + account.getName() + "' already exists");
        }
        
        return accountRepository.save(account);
    }
    
    /**
     * Delete account by ID.
     *
     * @param id the account ID
     * @throws IllegalArgumentException if account not found
     */
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account with ID " + id + " not found");
        }
        
        accountRepository.deleteById(id);
    }
    
    /**
     * Update account balance.
     *
     * @param id the account ID
     * @param newBalance the new balance
     * @return the updated account
     * @throws IllegalArgumentException if account not found
     */
    public Account updateAccountBalance(Long id, BigDecimal newBalance) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + id + " not found"));
        
        account.setBalance(newBalance);
        return accountRepository.save(account);
    }
    
    /**
     * Add amount to account balance.
     *
     * @param id the account ID
     * @param amount the amount to add
     * @return the updated account
     * @throws IllegalArgumentException if account not found
     */
    public Account addToAccountBalance(Long id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + id + " not found"));
        
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }
    
    /**
     * Subtract amount from account balance.
     *
     * @param id the account ID
     * @param amount the amount to subtract
     * @return the updated account
     * @throws IllegalArgumentException if account not found
     */
    public Account subtractFromAccountBalance(Long id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + id + " not found"));
        
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }
    
    /**
     * Check if account exists by name.
     *
     * @param name the account name to check
     * @return true if account exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean accountExistsByName(String name) {
        return accountRepository.existsByName(name);
    }
    
    /**
     * Check if account exists by name excluding a specific ID.
     *
     * @param name the account name to check
     * @param id the ID to exclude
     * @return true if account exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean accountExistsByNameAndIdNot(String name, Long id) {
        return accountRepository.existsByNameAndIdNot(name, id);
    }
}
