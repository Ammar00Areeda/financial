package com.financial.controller;

import com.financial.dto.RecurringExpenseCreateRequestDto;
import com.financial.dto.RecurringExpenseListDTO;
import com.financial.dto.RecurringExpenseResponseDto;
import com.financial.dto.RecurringExpenseUpdateRequestDto;
import com.financial.entity.Account;
import com.financial.entity.Category;
import com.financial.entity.RecurringExpense;
import com.financial.service.AccountService;
import com.financial.service.CategoryService;
import com.financial.service.RecurringExpenseService;
import jakarta.validation.Valid;
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
 * REST controller for RecurringExpense management operations.
 * Implements RecurringExpenseApi interface which contains all OpenAPI documentation.
 */
@Slf4j
@RestController
@RequestMapping("/api/recurring-expenses")
@RequiredArgsConstructor
public class RecurringExpenseController implements RecurringExpenseApi {

    private final RecurringExpenseService recurringExpenseService;
    private final AccountService accountService;
    private final CategoryService categoryService;

    @Override
    @GetMapping
    public ResponseEntity<Page<RecurringExpenseListDTO>> getAllRecurringExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<RecurringExpense> recurringExpenses = recurringExpenseService.getAllRecurringExpenses(pageable);
        Page<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.map(RecurringExpenseListDTO::fromEntity);
        
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Override
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesByAccount(@PathVariable Long accountId) {
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesByAccount(account.get());
        List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                .map(RecurringExpenseListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Override
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesByStatus(@PathVariable String status) {
        try {
            RecurringExpense.Status expenseStatus = RecurringExpense.Status.valueOf(status.toUpperCase());
            List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesByStatus(expenseStatus);
            List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                    .map(RecurringExpenseListDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(recurringExpenseDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/frequency/{frequency}")
    public ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesByFrequency(@PathVariable String frequency) {
        try {
            RecurringExpense.Frequency expenseFrequency = RecurringExpense.Frequency.valueOf(frequency.toUpperCase());
            List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesByFrequency(expenseFrequency);
            List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                    .map(RecurringExpenseListDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(recurringExpenseDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/due-today")
    public ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesDueToday() {
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesDueToday();
        List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                .map(RecurringExpenseListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Override
    @GetMapping("/overdue")
    public ResponseEntity<List<RecurringExpenseListDTO>> getOverdueRecurringExpenses() {
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getOverdueRecurringExpenses();
        List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                .map(RecurringExpenseListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Override
    @GetMapping("/due-soon")
    public ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesDueSoon(
            @RequestParam(defaultValue = "7") int daysAhead) {
        
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesDueSoon(daysAhead);
        List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                .map(RecurringExpenseListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Override
    @GetMapping("/auto-pay")
    public ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesWithAutoPay() {
        List<RecurringExpense> recurringExpenses = recurringExpenseService.getRecurringExpensesWithAutoPay();
        List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                .map(RecurringExpenseListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Override
    @GetMapping("/search/name")
    public ResponseEntity<List<RecurringExpenseListDTO>> searchRecurringExpensesByName(@RequestParam String name) {
        List<RecurringExpense> recurringExpenses = recurringExpenseService.searchRecurringExpensesByName(name);
        List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                .map(RecurringExpenseListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Override
    @GetMapping("/search/provider")
    public ResponseEntity<List<RecurringExpenseListDTO>> searchRecurringExpensesByProvider(@RequestParam String provider) {
        List<RecurringExpense> recurringExpenses = recurringExpenseService.searchRecurringExpensesByProvider(provider);
        List<RecurringExpenseListDTO> recurringExpenseDtos = recurringExpenses.stream()
                .map(RecurringExpenseListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recurringExpenseDtos);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<RecurringExpenseResponseDto> getRecurringExpenseById(@PathVariable Long id) {
        Optional<RecurringExpense> recurringExpense = recurringExpenseService.getRecurringExpenseById(id);
        return recurringExpense.map(RecurringExpenseResponseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping
    public ResponseEntity<RecurringExpenseResponseDto> createRecurringExpense(
            @Valid @RequestBody RecurringExpenseCreateRequestDto request) {
        try {
            // Validate account exists
            Optional<Account> account = accountService.getAccountById(request.getAccountId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            RecurringExpense recurringExpense = RecurringExpense.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .amount(request.getAmount())
                    .frequency(request.getFrequency())
                    .account(account.get())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .status(RecurringExpense.Status.ACTIVE)
                    .isAutoPay(request.getIsAutoPay() != null ? request.getIsAutoPay() : false)
                    .reminderDaysBefore(request.getReminderDaysBefore())
                    .provider(request.getProvider())
                    .referenceNumber(request.getReferenceNumber())
                    .notes(request.getNotes())
                    .build();
            
            // Validate category exists if provided
            if (request.getCategoryId() != null) {
                Optional<Category> category = categoryService.getCategoryById(request.getCategoryId());
                if (category.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                recurringExpense.setCategory(category.get());
            }
            
            RecurringExpense createdRecurringExpense = recurringExpenseService.createRecurringExpense(recurringExpense);
            RecurringExpenseResponseDto response = RecurringExpenseResponseDto.fromEntity(createdRecurringExpense);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating recurring expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<RecurringExpenseResponseDto> updateRecurringExpense(
            @PathVariable Long id,
            @Valid @RequestBody RecurringExpenseUpdateRequestDto request) {
        
        try {
            Optional<RecurringExpense> existingExpenseOpt = recurringExpenseService.getRecurringExpenseById(id);
            if (existingExpenseOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            RecurringExpense existingExpense = existingExpenseOpt.get();
            
            // Update fields from request DTO
            existingExpense.setName(request.getName());
            existingExpense.setDescription(request.getDescription());
            existingExpense.setAmount(request.getAmount());
            existingExpense.setFrequency(request.getFrequency());
            if (request.getStartDate() != null) {
                existingExpense.setStartDate(request.getStartDate());
            }
            existingExpense.setEndDate(request.getEndDate());
            if (request.getStatus() != null) {
                existingExpense.setStatus(request.getStatus());
            }
            if (request.getIsAutoPay() != null) {
                existingExpense.setIsAutoPay(request.getIsAutoPay());
            }
            existingExpense.setReminderDaysBefore(request.getReminderDaysBefore());
            existingExpense.setProvider(request.getProvider());
            existingExpense.setReferenceNumber(request.getReferenceNumber());
            existingExpense.setNotes(request.getNotes());
            
            // Validate account exists
            Optional<Account> account = accountService.getAccountById(request.getAccountId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            existingExpense.setAccount(account.get());
            
            // Validate category exists if provided
            if (request.getCategoryId() != null) {
                Optional<Category> category = categoryService.getCategoryById(request.getCategoryId());
                if (category.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                existingExpense.setCategory(category.get());
            } else {
                existingExpense.setCategory(null);
            }
            
            RecurringExpense updatedRecurringExpense = recurringExpenseService.updateRecurringExpense(existingExpense);
            RecurringExpenseResponseDto response = RecurringExpenseResponseDto.fromEntity(updatedRecurringExpense);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating recurring expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringExpense(@PathVariable Long id) {
        try {
            recurringExpenseService.deleteRecurringExpense(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== ACTION ENDPOINTS ==========

    @Override
    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<RecurringExpenseResponseDto> markAsPaid(@PathVariable Long id) {
        try {
            RecurringExpense recurringExpense = recurringExpenseService.markAsPaid(id);
            RecurringExpenseResponseDto response = RecurringExpenseResponseDto.fromEntity(recurringExpense);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/pause")
    public ResponseEntity<RecurringExpenseResponseDto> pauseRecurringExpense(@PathVariable Long id) {
        try {
            RecurringExpense recurringExpense = recurringExpenseService.pauseRecurringExpense(id);
            RecurringExpenseResponseDto response = RecurringExpenseResponseDto.fromEntity(recurringExpense);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/resume")
    public ResponseEntity<RecurringExpenseResponseDto> resumeRecurringExpense(@PathVariable Long id) {
        try {
            RecurringExpense recurringExpense = recurringExpenseService.resumeRecurringExpense(id);
            RecurringExpenseResponseDto response = RecurringExpenseResponseDto.fromEntity(recurringExpense);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RecurringExpenseResponseDto> cancelRecurringExpense(@PathVariable Long id) {
        try {
            RecurringExpense recurringExpense = recurringExpenseService.cancelRecurringExpense(id);
            RecurringExpenseResponseDto response = RecurringExpenseResponseDto.fromEntity(recurringExpense);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== REPORTING ENDPOINTS ==========

    @Override
    @GetMapping("/account/{accountId}/total-monthly")
    public ResponseEntity<BigDecimal> getTotalMonthlyRecurringExpenses(@PathVariable Long accountId) {
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal total = recurringExpenseService.calculateTotalMonthlyRecurringExpenses(account.get());
        return ResponseEntity.ok(total);
    }

    @Override
    @GetMapping("/account/{accountId}/total")
    public ResponseEntity<BigDecimal> getTotalRecurringExpenses(@PathVariable Long accountId) {
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal total = recurringExpenseService.calculateTotalRecurringExpenses(account.get());
        return ResponseEntity.ok(total);
    }

    @Override
    @PostMapping("/process-due")
    public ResponseEntity<Integer> processAllDueRecurringExpenses() {
        int processedCount = recurringExpenseService.processAllDueRecurringExpenses();
        return ResponseEntity.ok(processedCount);
    }
}
