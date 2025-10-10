package com.financial.controller;

import com.financial.dto.RecurringExpenseDto;
import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.RecurringExpense;
import com.financial.mapper.RecurringExpenseMapper;
import com.financial.service.AccountService;
import com.financial.service.CategoryService;
import com.financial.service.RecurringExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * REST controller for RecurringExpense management operations.
 */
@RestController
@RequestMapping("/api/recurring-expenses")
@RequiredArgsConstructor
@Tag(name = "Recurring Expenses", description = "Recurring expense management operations")
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final RecurringExpenseMapper recurringExpenseMapper;

    @Operation(
            summary = "Get all recurring expenses",
            description = "Retrieve a list of all recurring expenses with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<RecurringExpenseDto>> getAllRecurringExpenses(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort criteria (field,direction)") @RequestParam(defaultValue = "name,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<RecurringExpense> recurringExpenses = recurringExpenseService.getAllRecurringExpenses(pageable);
        Page<RecurringExpenseDto> recurringExpenseDtos = recurringExpenses.map(recurringExpenseMapper::toDto);
        
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Operation(
            summary = "Get recurring expenses by account",
            description = "Retrieve recurring expenses for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<RecurringExpenseDto>> getRecurringExpensesByAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesByAccount(account.get());
        List<RecurringExpenseDto> recurringExpenseDtos = recurringExpenses.stream()
                .map(recurringExpenseMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Operation(
            summary = "Get recurring expenses by status",
            description = "Retrieve recurring expenses filtered by status (ACTIVE, PAUSED, CANCELLED, COMPLETED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses by status"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RecurringExpense>> getRecurringExpensesByStatus(
            @Parameter(description = "Status", required = true) @PathVariable String status) {
        
        try {
            RecurringExpense.Status expenseStatus = RecurringExpense.Status.valueOf(status.toUpperCase());
            List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesByStatus(expenseStatus);
            return ResponseEntity.ok(recurringExpenses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get recurring expenses by frequency",
            description = "Retrieve recurring expenses filtered by frequency (DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses by frequency"),
            @ApiResponse(responseCode = "400", description = "Invalid frequency"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/frequency/{frequency}")
    public ResponseEntity<List<RecurringExpense>> getRecurringExpensesByFrequency(
            @Parameter(description = "Frequency", required = true) @PathVariable String frequency) {
        
        try {
            RecurringExpense.Frequency expenseFrequency = RecurringExpense.Frequency.valueOf(frequency.toUpperCase());
            List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesByFrequency(expenseFrequency);
            return ResponseEntity.ok(recurringExpenses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get recurring expenses due today",
            description = "Retrieve recurring expenses that are due today"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses due today"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/due-today")
    public ResponseEntity<List<RecurringExpense>> getRecurringExpensesDueToday() {
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesDueToday();
        return ResponseEntity.ok(recurringExpenses);
    }

    @Operation(
            summary = "Get overdue recurring expenses",
            description = "Retrieve recurring expenses that are overdue"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/overdue")
    public ResponseEntity<List<RecurringExpense>> getOverdueRecurringExpenses() {
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getOverdueRecurringExpenses();
        return ResponseEntity.ok(recurringExpenses);
    }

    @Operation(
            summary = "Get recurring expenses due soon",
            description = "Retrieve recurring expenses that are due within the specified number of days"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses due soon"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/due-soon")
    public ResponseEntity<List<RecurringExpense>> getRecurringExpensesDueSoon(
            @Parameter(description = "Number of days ahead to check") @RequestParam(defaultValue = "7") int daysAhead) {
        
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesDueSoon(daysAhead);
        return ResponseEntity.ok(recurringExpenses);
    }

    @Operation(
            summary = "Get recurring expenses with auto-pay",
            description = "Retrieve recurring expenses that have auto-pay enabled"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses with auto-pay"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/auto-pay")
    public ResponseEntity<List<RecurringExpense>> getRecurringExpensesWithAutoPay() {
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesWithAutoPay();
        return ResponseEntity.ok(recurringExpenses);
    }

    @Operation(
            summary = "Search recurring expenses by name",
            description = "Search recurring expenses by name containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search/name")
    public ResponseEntity<List<RecurringExpense>> searchRecurringExpensesByName(
            @Parameter(description = "Name pattern to search for") @RequestParam String name) {
        
        List<RecurringExpense> recurringExpenses = recurringExpenseService.searchRecurringExpensesByName(name);
        return ResponseEntity.ok(recurringExpenses);
    }

    @Operation(
            summary = "Search recurring expenses by provider",
            description = "Search recurring expenses by provider containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search/provider")
    public ResponseEntity<List<RecurringExpense>> searchRecurringExpensesByProvider(
            @Parameter(description = "Provider pattern to search for") @RequestParam String provider) {
        
        List<RecurringExpense> recurringExpenses = recurringExpenseService.searchRecurringExpensesByProvider(provider);
        return ResponseEntity.ok(recurringExpenses);
    }

    @Operation(
            summary = "Get recurring expense by ID",
            description = "Retrieve a specific recurring expense by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense found"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecurringExpense> getRecurringExpenseById(
            @Parameter(description = "Recurring expense ID", required = true) @PathVariable Long id) {
        
        Optional<RecurringExpense> recurringExpense = recurringExpenseService.getRecurringExpenseById(id);
        return recurringExpense.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create a new recurring expense",
            description = "Create a new recurring expense in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recurring expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Account or category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<RecurringExpense> createRecurringExpense(@Valid @RequestBody RecurringExpense recurringExpense) {
        // Validate account exists
        if (recurringExpense.getAccount() != null && recurringExpense.getAccount().getId() != null) {
            Optional<Account> account = accountService.getAccountById(recurringExpense.getAccount().getId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            recurringExpense.setAccount(account.get());
        }
        
        // Validate category exists if provided
        if (recurringExpense.getCategory() != null && recurringExpense.getCategory().getId() != null) {
            Optional<Category> category = categoryService.getCategoryById(recurringExpense.getCategory().getId());
            if (category.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            recurringExpense.setCategory(category.get());
        }
        
        RecurringExpense createdRecurringExpense = recurringExpenseService.createRecurringExpense(recurringExpense);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecurringExpense);
    }

    @Operation(
            summary = "Update recurring expense",
            description = "Update an existing recurring expense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RecurringExpense> updateRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RecurringExpense recurringExpense) {
        
        if (!recurringExpenseService.getRecurringExpenseById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // Validate account exists if provided
        if (recurringExpense.getAccount() != null && recurringExpense.getAccount().getId() != null) {
            Optional<Account> account = accountService.getAccountById(recurringExpense.getAccount().getId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            recurringExpense.setAccount(account.get());
        }
        
        // Validate category exists if provided
        if (recurringExpense.getCategory() != null && recurringExpense.getCategory().getId() != null) {
            Optional<Category> category = categoryService.getCategoryById(recurringExpense.getCategory().getId());
            if (category.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            recurringExpense.setCategory(category.get());
        }
        
        recurringExpense.setId(id);
        RecurringExpense updatedRecurringExpense = recurringExpenseService.updateRecurringExpense(recurringExpense);
        return ResponseEntity.ok(updatedRecurringExpense);
    }

    @Operation(
            summary = "Delete recurring expense",
            description = "Delete a recurring expense from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recurring expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) @PathVariable Long id) {
        
        try {
            recurringExpenseService.deleteRecurringExpense(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== ACTION ENDPOINTS ==========

    @Operation(
            summary = "Mark recurring expense as paid",
            description = "Mark a recurring expense as paid and create a transaction record"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense marked as paid successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<RecurringExpense> markAsPaid(
            @Parameter(description = "Recurring expense ID", required = true) @PathVariable Long id) {
        
        try {
            RecurringExpense recurringExpense = recurringExpenseService.markAsPaid(id);
            return ResponseEntity.ok(recurringExpense);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Pause recurring expense",
            description = "Pause a recurring expense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense paused successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/pause")
    public ResponseEntity<RecurringExpense> pauseRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) @PathVariable Long id) {
        
        try {
            RecurringExpense recurringExpense = recurringExpenseService.pauseRecurringExpense(id);
            return ResponseEntity.ok(recurringExpense);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Resume recurring expense",
            description = "Resume a paused recurring expense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense resumed successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/resume")
    public ResponseEntity<RecurringExpense> resumeRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) @PathVariable Long id) {
        
        try {
            RecurringExpense recurringExpense = recurringExpenseService.resumeRecurringExpense(id);
            return ResponseEntity.ok(recurringExpense);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Cancel recurring expense",
            description = "Cancel a recurring expense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RecurringExpense> cancelRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) @PathVariable Long id) {
        
        try {
            RecurringExpense recurringExpense = recurringExpenseService.cancelRecurringExpense(id);
            return ResponseEntity.ok(recurringExpense);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== REPORTING ENDPOINTS ==========

    @Operation(
            summary = "Get total monthly recurring expenses for account",
            description = "Calculate total monthly recurring expenses for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total monthly recurring expenses"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}/total-monthly")
    public ResponseEntity<BigDecimal> getTotalMonthlyRecurringExpenses(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal total = recurringExpenseService.calculateTotalMonthlyRecurringExpenses(account.get());
        return ResponseEntity.ok(total);
    }

    @Operation(
            summary = "Get total recurring expenses for account",
            description = "Calculate total recurring expenses for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total recurring expenses"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}/total")
    public ResponseEntity<BigDecimal> getTotalRecurringExpenses(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal total = recurringExpenseService.calculateTotalRecurringExpenses(account.get());
        return ResponseEntity.ok(total);
    }

    @Operation(
            summary = "Process all due recurring expenses",
            description = "Process all recurring expenses that are due today (for scheduled jobs)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed due recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/process-due")
    public ResponseEntity<Integer> processAllDueRecurringExpenses() {
        int processedCount = recurringExpenseService.processAllDueRecurringExpenses();
        return ResponseEntity.ok(processedCount);
    }
}
