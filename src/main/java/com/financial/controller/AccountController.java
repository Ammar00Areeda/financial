package com.financial.controller;

import com.financial.dto.AccountDto;
import com.financial.dto.AccountListDTO;
import com.financial.dto.AccountResponseDto;
import com.financial.entity.Account;
import com.financial.mapper.AccountMapper;
import com.financial.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Request DTO for creating accounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CreateAccountRequest {
    @NotBlank(message = "Account name is required")
    private String name;
    
    @NotBlank(message = "Account type is required")
    private String type;
    
    @NotNull(message = "Initial balance is required")
    private BigDecimal initialBalance;
    
    @NotBlank(message = "Currency is required")
    private String currency;
}

/**
 * REST controller for Account management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Operation(
            summary = "Get all accounts",
            description = "Retrieve a list of all accounts"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<AccountListDTO>> getAllAccounts() {
        
        List<Account> accounts = accountService.getAllAccounts();
        
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(accountDtos);
    }

    @Operation(
            summary = "Create a new account",
            description = "Create a new account with initial balance"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<AccountResponseDto.AccountDto> createAccount(
            @Parameter(description = "Account creation request") @Valid @RequestBody CreateAccountRequest request) {
        
        try {
            Account account = Account.builder()
                    .name(request.getName())
                    .type(Account.AccountType.valueOf(request.getType().toUpperCase()))
                    .balance(request.getInitialBalance())
                    .currency(request.getCurrency())
                    .status(Account.AccountStatus.ACTIVE)
                    .includeInBalance(true)
                    .build();
            
            Account createdAccount = accountService.createAccount(account);
            
            AccountResponseDto.AccountDto response = AccountResponseDto.AccountDto.builder()
                    .id(createdAccount.getId().toString())
                    .name(createdAccount.getName())
                    .type(createdAccount.getType().toString().toLowerCase())
                    .balance(createdAccount.getBalance())
                    .currency(createdAccount.getCurrency())
                    .institution(createdAccount.getBankName())
                    .createdAt(createdAccount.getCreatedAt())
                    .updatedAt(createdAccount.getUpdatedAt())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get all active accounts",
            description = "Retrieve a list of all active accounts"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active")
    public ResponseEntity<List<AccountListDTO>> getAllActiveAccounts() {
        List<Account> accounts = accountService.getAllActiveAccounts();
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }

    @Operation(
            summary = "Get accounts by type",
            description = "Retrieve accounts filtered by type (WALLET, BANK_ACCOUNT, SAVINGS, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts by type"),
            @ApiResponse(responseCode = "400", description = "Invalid account type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AccountListDTO>> getAccountsByType(
            @Parameter(description = "Account type", required = true) @PathVariable String type) {
        
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

    @Operation(
            summary = "Get active accounts by type",
            description = "Retrieve active accounts filtered by type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active accounts by type"),
            @ApiResponse(responseCode = "400", description = "Invalid account type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active/type/{type}")
    public ResponseEntity<List<AccountListDTO>> getActiveAccountsByType(
            @Parameter(description = "Account type", required = true) @PathVariable String type) {
        
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

    @Operation(
            summary = "Get accounts included in balance",
            description = "Retrieve accounts that are included in total balance calculation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts included in balance"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/included-in-balance")
    public ResponseEntity<List<AccountListDTO>> getAccountsIncludedInBalance() {
        List<Account> accounts = accountService.getAccountsIncludedInBalance();
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }

    @Operation(
            summary = "Get total balance",
            description = "Calculate total balance across all accounts included in balance calculation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total balance"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/total-balance")
    public ResponseEntity<BigDecimal> getTotalBalance() {
        BigDecimal totalBalance = accountService.calculateTotalBalance();
        return ResponseEntity.ok(totalBalance);
    }

    @Operation(
            summary = "Get total balance by type",
            description = "Calculate total balance for a specific account type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total balance by type"),
            @ApiResponse(responseCode = "400", description = "Invalid account type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/total-balance/type/{type}")
    public ResponseEntity<BigDecimal> getTotalBalanceByType(
            @Parameter(description = "Account type", required = true) @PathVariable String type) {
        
        try {
            Account.AccountType accountType = Account.AccountType.valueOf(type.toUpperCase());
            BigDecimal totalBalance = accountService.calculateTotalBalanceByType(accountType);
            return ResponseEntity.ok(totalBalance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get account by ID",
            description = "Retrieve a specific account by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id) {
        
        Optional<Account> account = accountService.getAccountById(id);
        return account.map(accountMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Search accounts by name",
            description = "Search accounts by name containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<AccountListDTO>> searchAccountsByName(
            @Parameter(description = "Name pattern to search for") @RequestParam String name) {
        
        List<Account> accounts = accountService.searchAccountsByName(name);
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }

    @Operation(
            summary = "Search active accounts by name",
            description = "Search active accounts by name containing the provided text"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching active accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active/search")
    public ResponseEntity<List<AccountListDTO>> searchActiveAccountsByName(
            @Parameter(description = "Name pattern to search for") @RequestParam String name) {
        
        List<Account> accounts = accountService.searchActiveAccountsByName(name);
        List<AccountListDTO> accountDtos = accounts.stream()
                .map(AccountListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }


    @Operation(
            summary = "Update account",
            description = "Update an existing account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or duplicate name"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AccountDto accountDto) {
        
        try {
            // Fetch existing account
            Optional<Account> existingAccountOpt = accountService.getAccountById(id);
            if (existingAccountOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Update fields from DTO
            Account existingAccount = existingAccountOpt.get();
            accountMapper.updateEntityFromDto(existingAccount, accountDto);
            
            // Save updated account
            Account updatedAccount = accountService.updateAccount(existingAccount);
            return ResponseEntity.ok(accountMapper.toDto(updatedAccount));
        } catch (IllegalArgumentException e) {
            // Duplicate name error
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Log the actual exception for debugging
            log.error("Error updating account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Update account balance",
            description = "Update the balance of a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account balance updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/balance")
    public ResponseEntity<AccountDto> updateAccountBalance(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id,
            @Parameter(description = "New balance") @RequestParam BigDecimal balance) {
        
        try {
            Account account = accountService.updateAccountBalance(id, balance);
            return ResponseEntity.ok(accountMapper.toDto(account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Add amount to account balance",
            description = "Add a specific amount to an account's balance"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amount added successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/add")
    public ResponseEntity<AccountDto> addToAccountBalance(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id,
            @Parameter(description = "Amount to add") @RequestParam BigDecimal amount) {
        
        try {
            Account account = accountService.addToAccountBalance(id, amount);
            return ResponseEntity.ok(accountMapper.toDto(account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Subtract amount from account balance",
            description = "Subtract a specific amount from an account's balance"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amount subtracted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/subtract")
    public ResponseEntity<AccountDto> subtractFromAccountBalance(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id,
            @Parameter(description = "Amount to subtract") @RequestParam BigDecimal amount) {
        
        try {
            Account account = accountService.subtractFromAccountBalance(id, amount);
            return ResponseEntity.ok(accountMapper.toDto(account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Delete account",
            description = "Delete an account from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id) {
        
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
