package com.financial.controller;

import com.financial.dto.TransactionDto;
import com.financial.dto.TransactionListDTO;
import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.Transaction;
import com.financial.mapper.TransactionMapper;
import com.financial.service.AccountService;
import com.financial.service.CategoryService;
import com.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Transaction management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionMapper transactionMapper;

    @Operation(
            summary = "Get last 5 transactions",
            description = "Retrieve the last 5 transactions ordered by date descending"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved last 5 transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/last-5")
    public ResponseEntity<List<TransactionListDTO>> getLast5Transactions() {
        List<Transaction> transactions = transactionService.getLast5Transactions();
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get last N transactions",
            description = "Retrieve the last N transactions ordered by date descending"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved last N transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/last")
    public ResponseEntity<List<TransactionListDTO>> getLastTransactions(
            @Parameter(description = "Number of transactions to retrieve") @RequestParam(defaultValue = "10") int limit) {
        List<Transaction> transactions = transactionService.getLastTransactions(limit);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get all transactions",
            description = "Retrieve a list of all transactions with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<TransactionListDTO>> getAllTransactions(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort criteria (field,direction)") @RequestParam(defaultValue = "transactionDate,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);
        Page<TransactionListDTO> transactionDtos = transactions.map(TransactionListDTO::fromEntity);
        
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get transactions by account",
            description = "Retrieve transactions for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Transaction> transactions = transactionService.getTransactionsByAccount(account.get());
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get transactions by account with pagination",
            description = "Retrieve transactions for a specific account with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}/paginated")
    public ResponseEntity<Page<TransactionListDTO>> getTransactionsByAccountPaginated(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort criteria (field,direction)") @RequestParam(defaultValue = "transactionDate,desc") String sort) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Transaction> transactions = transactionService.getTransactionsByAccount(account.get(), pageable);
        Page<TransactionListDTO> transactionDtos = transactions.map(TransactionListDTO::fromEntity);
        
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get last N transactions by account",
            description = "Retrieve the last N transactions for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved last N transactions by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}/last")
    public ResponseEntity<List<TransactionListDTO>> getLastTransactionsByAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId,
            @Parameter(description = "Number of transactions to retrieve") @RequestParam(defaultValue = "10") int limit) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Transaction> transactions = transactionService.getLastTransactionsByAccount(account.get(), limit);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get transactions by type",
            description = "Retrieve transactions filtered by type (INCOME, EXPENSE, TRANSFER)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by type"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByType(
            @Parameter(description = "Transaction type", required = true) @PathVariable String type) {
        
        try {
            Transaction.TransactionType transactionType = Transaction.TransactionType.valueOf(type.toUpperCase());
            List<Transaction> transactions = transactionService.getTransactionsByType(transactionType);
            List<TransactionListDTO> transactionDtos = transactions.stream()
                    .map(TransactionListDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(transactionDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get transactions by category",
            description = "Retrieve transactions filtered by category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by category"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId) {
        
        Optional<Category> category = categoryService.getCategoryById(categoryId);
        if (category.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Transaction> transactions = transactionService.getTransactionsByCategory(category.get());
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get transactions by date range",
            description = "Retrieve transactions within a specific date range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get transactions by amount range",
            description = "Retrieve transactions within a specific amount range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by amount range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/amount-range")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByAmountRange(
            @Parameter(description = "Minimum amount") @RequestParam BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam BigDecimal maxAmount) {
        
        List<Transaction> transactions = transactionService.getTransactionsByAmountRange(minAmount, maxAmount);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Search transactions by description",
            description = "Search transactions by description containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<TransactionListDTO>> searchTransactionsByDescription(
            @Parameter(description = "Description pattern to search for") @RequestParam String description) {
        
        List<Transaction> transactions = transactionService.searchTransactionsByDescription(description);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Retrieve a specific transaction by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(
            @Parameter(description = "Transaction ID", required = true) @PathVariable Long id) {
        
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        return transaction.map(transactionMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create a new transaction",
            description = "Create a new transaction in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Account or category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        try {
            Transaction transaction = transactionMapper.toEntity(transactionDto);
            
            // Validate account exists
            if (transactionDto.getAccountId() != null) {
                Optional<Account> account = accountService.getAccountById(transactionDto.getAccountId());
                if (account.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                transaction.setAccount(account.get());
            }
            
            // Validate category exists if provided
            if (transactionDto.getCategoryId() != null) {
                Optional<Category> category = categoryService.getCategoryById(transactionDto.getCategoryId());
                if (category.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                transaction.setCategory(category.get());
            }
            
            // Validate transfer account exists if provided
            if (transactionDto.getTransferToAccountId() != null) {
                Optional<Account> transferAccount = accountService.getAccountById(transactionDto.getTransferToAccountId());
                if (transferAccount.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                transaction.setTransferToAccount(transferAccount.get());
            }
            
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            TransactionDto createdTransactionDto = transactionMapper.toDto(createdTransaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTransactionDto);
        } catch (IllegalArgumentException e) {
            // Check if it's a not found error
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Log and return internal server error for unexpected exceptions
            log.error("Error creating transaction: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Update transaction",
            description = "Update an existing transaction"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @Parameter(description = "Transaction ID", required = true) @PathVariable Long id,
            @Valid @RequestBody TransactionDto transactionDto) {
        
        try {
            // Fetch existing transaction
            Optional<Transaction> existingTransactionOpt = transactionService.getTransactionById(id);
            if (existingTransactionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Transaction existingTransaction = existingTransactionOpt.get();
            
            // Update fields from DTO
            transactionMapper.updateEntityFromDto(existingTransaction, transactionDto);
            
            // Validate and set account if changed
            if (transactionDto.getAccountId() != null) {
                Optional<Account> account = accountService.getAccountById(transactionDto.getAccountId());
                if (account.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                existingTransaction.setAccount(account.get());
            }
            
            // Validate and set category if provided
            if (transactionDto.getCategoryId() != null) {
                Optional<Category> category = categoryService.getCategoryById(transactionDto.getCategoryId());
                if (category.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                existingTransaction.setCategory(category.get());
            } else {
                existingTransaction.setCategory(null);
            }
            
            // Validate and set transfer account if provided
            if (transactionDto.getTransferToAccountId() != null) {
                Optional<Account> transferAccount = accountService.getAccountById(transactionDto.getTransferToAccountId());
                if (transferAccount.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                existingTransaction.setTransferToAccount(transferAccount.get());
            } else {
                existingTransaction.setTransferToAccount(null);
            }
            
            Transaction updatedTransaction = transactionService.updateTransaction(existingTransaction);
            TransactionDto updatedTransactionDto = transactionMapper.toDto(updatedTransaction);
            return ResponseEntity.ok(updatedTransactionDto);
        } catch (IllegalArgumentException e) {
            // Check if it's a not found error
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Log and return internal server error for unexpected exceptions
            log.error("Error updating transaction: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Delete transaction",
            description = "Delete a transaction from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Transaction ID", required = true) @PathVariable Long id) {
        
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Get total amount by type",
            description = "Calculate total amount for a specific transaction type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total amount by type"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/total-amount/type/{type}")
    public ResponseEntity<BigDecimal> getTotalAmountByType(
            @Parameter(description = "Transaction type", required = true) @PathVariable String type) {
        
        try {
            Transaction.TransactionType transactionType = Transaction.TransactionType.valueOf(type.toUpperCase());
            BigDecimal totalAmount = transactionService.calculateTotalAmountByType(transactionType);
            return ResponseEntity.ok(totalAmount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get total income for account",
            description = "Calculate total income amount for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total income"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}/total-income")
    public ResponseEntity<BigDecimal> getTotalIncomeByAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal totalIncome = transactionService.calculateTotalIncomeByAccount(account.get());
        return ResponseEntity.ok(totalIncome);
    }

    @Operation(
            summary = "Get total expense for account",
            description = "Calculate total expense amount for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total expense"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}/total-expense")
    public ResponseEntity<BigDecimal> getTotalExpenseByAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal totalExpense = transactionService.calculateTotalExpenseByAccount(account.get());
        return ResponseEntity.ok(totalExpense);
    }
}
