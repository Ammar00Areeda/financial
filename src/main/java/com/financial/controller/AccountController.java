package com.financial.controller;

import com.financial.dto.AccountCreateRequestDto;
import com.financial.dto.AccountDto;
import com.financial.dto.AccountListDTO;
import com.financial.dto.AccountUpdateRequestDto;
import com.financial.entity.Account;
import com.financial.mapper.AccountMapper;
import com.financial.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Account management operations.
 * Implements AccountApi interface which contains all OpenAPI documentation.
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController implements AccountApi {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Override
    @GetMapping
    public ResponseEntity<List<AccountListDTO>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }

    @Override
    @GetMapping("/active")
    public ResponseEntity<List<AccountListDTO>> getAllActiveAccounts() {
        List<Account> accounts = accountService.getAllActiveAccounts();
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }

    @Override
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AccountListDTO>> getAccountsByType(@PathVariable String type) {
        try {
            Account.AccountType accountType = Account.AccountType.valueOf(type.toUpperCase());
            List<Account> accounts = accountService.getAccountsByType(accountType);
            List<AccountListDTO> accountDtos = accounts.stream()
                    .map(AccountListDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(accountDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/active/type/{type}")
    public ResponseEntity<List<AccountListDTO>> getActiveAccountsByType(@PathVariable String type) {
        try {
            Account.AccountType accountType = Account.AccountType.valueOf(type.toUpperCase());
            List<Account> accounts = accountService.getActiveAccountsByType(accountType);
            List<AccountListDTO> accountDtos = accounts.stream()
                    .map(AccountListDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(accountDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/included-in-balance")
    public ResponseEntity<List<AccountListDTO>> getAccountsIncludedInBalance() {
        List<Account> accounts = accountService.getAccountsIncludedInBalance();
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }

    @Override
    @GetMapping("/total-balance")
    public ResponseEntity<BigDecimal> getTotalBalance() {
        BigDecimal totalBalance = accountService.calculateTotalBalance();
        return ResponseEntity.ok(totalBalance);
    }

    @Override
    @GetMapping("/total-balance/type/{type}")
    public ResponseEntity<BigDecimal> getTotalBalanceByType(@PathVariable String type) {
        try {
            Account.AccountType accountType = Account.AccountType.valueOf(type.toUpperCase());
            BigDecimal totalBalance = accountService.calculateTotalBalanceByType(accountType);
            return ResponseEntity.ok(totalBalance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
        Optional<Account> account = accountService.getAccountById(id);
        return account.map(accountMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<List<AccountListDTO>> searchAccountsByName(@RequestParam String name) {
        List<Account> accounts = accountService.searchAccountsByName(name);
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }

    @Override
    @GetMapping("/active/search")
    public ResponseEntity<List<AccountListDTO>> searchActiveAccountsByName(@RequestParam String name) {
        List<Account> accounts = accountService.searchActiveAccountsByName(name);
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }

    @Override
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountCreateRequestDto request) {
        try {
            Account account = Account.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .type(request.getType())
                    .balance(request.getInitialBalance())
                    .currency(request.getCurrency())
                    .accountNumber(request.getAccountNumber())
                    .bankName(request.getBankName())
                    .color(request.getColor())
                    .icon(request.getIcon())
                    .status(Account.AccountStatus.ACTIVE)
                    .includeInBalance(request.getIncludeInBalance() != null ? request.getIncludeInBalance() : true)
                    .build();
            
            Account createdAccount = accountService.createAccount(account);
            AccountDto response = accountMapper.toDto(createdAccount);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Error creating account: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequestDto request) {
        
        try {
            // Fetch existing account
            Optional<Account> existingAccountOpt = accountService.getAccountById(id);
            if (existingAccountOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Update fields from DTO
            Account existingAccount = existingAccountOpt.get();
            existingAccount.setName(request.getName());
            existingAccount.setDescription(request.getDescription());
            if (request.getType() != null) {
                existingAccount.setType(request.getType());
            }
            if (request.getBalance() != null) {
                existingAccount.setBalance(request.getBalance());
            }
            if (request.getCurrency() != null) {
                existingAccount.setCurrency(request.getCurrency());
            }
            if (request.getAccountNumber() != null) {
                existingAccount.setAccountNumber(request.getAccountNumber());
            }
            if (request.getBankName() != null) {
                existingAccount.setBankName(request.getBankName());
            }
            if (request.getStatus() != null) {
                existingAccount.setStatus(request.getStatus());
            }
            if (request.getColor() != null) {
                existingAccount.setColor(request.getColor());
            }
            if (request.getIcon() != null) {
                existingAccount.setIcon(request.getIcon());
            }
            if (request.getIncludeInBalance() != null) {
                existingAccount.setIncludeInBalance(request.getIncludeInBalance());
            }
            
            // Save updated account
            Account updatedAccount = accountService.updateAccount(existingAccount);
            return ResponseEntity.ok(accountMapper.toDto(updatedAccount));
        } catch (IllegalArgumentException e) {
            // Duplicate name error
            log.error("Error updating account: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Log the actual exception for debugging
            log.error("Error updating account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @PatchMapping("/{id}/balance")
    public ResponseEntity<AccountDto> updateAccountBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal balance) {
        
        try {
            Account account = accountService.updateAccountBalance(id, balance);
            return ResponseEntity.ok(accountMapper.toDto(account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/add")
    public ResponseEntity<AccountDto> addToAccountBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        
        try {
            Account account = accountService.addToAccountBalance(id, amount);
            return ResponseEntity.ok(accountMapper.toDto(account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/subtract")
    public ResponseEntity<AccountDto> subtractFromAccountBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        
        try {
            Account account = accountService.subtractFromAccountBalance(id, amount);
            return ResponseEntity.ok(accountMapper.toDto(account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
