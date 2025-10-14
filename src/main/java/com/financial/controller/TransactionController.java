package com.financial.controller;

import com.financial.dto.TransactionCreateRequestDto;
import com.financial.dto.TransactionDto;
import com.financial.dto.TransactionListDTO;
import com.financial.dto.TransactionUpdateRequestDto;
import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.Transaction;
import com.financial.mapper.TransactionMapper;
import com.financial.service.AccountService;
import com.financial.service.CategoryService;
import com.financial.service.TransactionService;
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
 * Implements TransactionApi interface which contains all OpenAPI documentation.
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController implements TransactionApi {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionMapper transactionMapper;

    @Override
    @GetMapping("/last-5")
    public ResponseEntity<List<TransactionListDTO>> getLast5Transactions() {
        List<Transaction> transactions = transactionService.getLast5Transactions();
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Override
    @GetMapping("/last")
    public ResponseEntity<List<TransactionListDTO>> getLastTransactions(
            @RequestParam(defaultValue = "10") int limit) {
        List<Transaction> transactions = transactionService.getLastTransactions(limit);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<TransactionListDTO>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);
        Page<TransactionListDTO> transactionDtos = transactions.map(TransactionListDTO::fromEntity);
        
        return ResponseEntity.ok(transactionDtos);
    }

    @Override
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByAccount(@PathVariable Long accountId) {
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

    @Override
    @GetMapping("/account/{accountId}/paginated")
    public ResponseEntity<Page<TransactionListDTO>> getTransactionsByAccountPaginated(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String sort) {
        
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

    @Override
    @GetMapping("/account/{accountId}/last")
    public ResponseEntity<List<TransactionListDTO>> getLastTransactionsByAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "10") int limit) {
        
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

    @Override
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByType(@PathVariable String type) {
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

    @Override
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByCategory(@PathVariable Long categoryId) {
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

    @Override
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Override
    @GetMapping("/amount-range")
    public ResponseEntity<List<TransactionListDTO>> getTransactionsByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        
        List<Transaction> transactions = transactionService.getTransactionsByAmountRange(minAmount, maxAmount);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<List<TransactionListDTO>> searchTransactionsByDescription(@RequestParam String description) {
        List<Transaction> transactions = transactionService.searchTransactionsByDescription(description);
        List<TransactionListDTO> transactionDtos = transactions.stream()
                .map(TransactionListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDtos);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        return transaction.map(transactionMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionCreateRequestDto request) {
        try {
            // Validate account exists
            Optional<Account> account = accountService.getAccountById(request.getAccountId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Transaction transaction = Transaction.builder()
                    .description(request.getDescription())
                    .amount(request.getAmount())
                    .type(request.getType())
                    .account(account.get())
                    .transactionDate(request.getTransactionDate())
                    .notes(request.getNotes())
                    .location(request.getLocation())
                    .referenceNumber(request.getReferenceNumber())
                    .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                    .recurringFrequency(request.getRecurringFrequency())
                    .recurringEndDate(request.getRecurringEndDate())
                    .build();
            
            // Validate category exists if provided
            if (request.getCategoryId() != null) {
                Optional<Category> category = categoryService.getCategoryById(request.getCategoryId());
                if (category.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                transaction.setCategory(category.get());
            }
            
            // Validate transfer account exists if provided
            if (request.getTransferToAccountId() != null) {
                Optional<Account> transferAccount = accountService.getAccountById(request.getTransferToAccountId());
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
            log.error("Error creating transaction: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Log and return internal server error for unexpected exceptions
            log.error("Error creating transaction: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequestDto request) {
        
        try {
            // Fetch existing transaction
            Optional<Transaction> existingTransactionOpt = transactionService.getTransactionById(id);
            if (existingTransactionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Transaction existingTransaction = existingTransactionOpt.get();
            
            // Update fields from request DTO
            existingTransaction.setDescription(request.getDescription());
            existingTransaction.setAmount(request.getAmount());
            existingTransaction.setType(request.getType());
            existingTransaction.setTransactionDate(request.getTransactionDate());
            existingTransaction.setNotes(request.getNotes());
            existingTransaction.setLocation(request.getLocation());
            existingTransaction.setReferenceNumber(request.getReferenceNumber());
            if (request.getIsRecurring() != null) {
                existingTransaction.setIsRecurring(request.getIsRecurring());
            }
            existingTransaction.setRecurringFrequency(request.getRecurringFrequency());
            existingTransaction.setRecurringEndDate(request.getRecurringEndDate());
            
            // Validate and set account if changed
            Optional<Account> account = accountService.getAccountById(request.getAccountId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            existingTransaction.setAccount(account.get());
            
            // Validate and set category if provided
            if (request.getCategoryId() != null) {
                Optional<Category> category = categoryService.getCategoryById(request.getCategoryId());
                if (category.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                existingTransaction.setCategory(category.get());
            } else {
                existingTransaction.setCategory(null);
            }
            
            // Validate and set transfer account if provided
            if (request.getTransferToAccountId() != null) {
                Optional<Account> transferAccount = accountService.getAccountById(request.getTransferToAccountId());
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
            log.error("Error updating transaction: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Log and return internal server error for unexpected exceptions
            log.error("Error updating transaction: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @GetMapping("/total-amount/type/{type}")
    public ResponseEntity<BigDecimal> getTotalAmountByType(@PathVariable String type) {
        try {
            Transaction.TransactionType transactionType = Transaction.TransactionType.valueOf(type.toUpperCase());
            BigDecimal totalAmount = transactionService.calculateTotalAmountByType(transactionType);
            return ResponseEntity.ok(totalAmount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/account/{accountId}/total-income")
    public ResponseEntity<BigDecimal> getTotalIncomeByAccount(@PathVariable Long accountId) {
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal totalIncome = transactionService.calculateTotalIncomeByAccount(account.get());
        return ResponseEntity.ok(totalIncome);
    }

    @Override
    @GetMapping("/account/{accountId}/total-expense")
    public ResponseEntity<BigDecimal> getTotalExpenseByAccount(@PathVariable Long accountId) {
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal totalExpense = transactionService.calculateTotalExpenseByAccount(account.get());
        return ResponseEntity.ok(totalExpense);
    }
}
