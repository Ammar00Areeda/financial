package com.financial.controller;

import com.financial.dto.LoanDto;
import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.mapper.LoanMapper;
import com.financial.service.AccountService;
import com.financial.service.LoanService;
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
 * REST controller for Loan management operations and reporting.
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan management and reporting operations")
public class LoanController {

    private final LoanService loanService;
    private final AccountService accountService;
    private final LoanMapper loanMapper;

    // ========== BASIC CRUD OPERATIONS ==========

    @Operation(
            summary = "Get all loans",
            description = "Retrieve a list of all loans with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<LoanDto>> getAllLoans(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort criteria (field,direction)") @RequestParam(defaultValue = "loanDate,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Loan> loans = loanService.getAllLoans(pageable);
        Page<LoanDto> loanDtos = loans.map(loanMapper::toDto);
        
        return ResponseEntity.ok(loanDtos);
    }

    @Operation(
            summary = "Get loans by type",
            description = "Retrieve loans filtered by type (LENT or BORROWED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by type"),
            @ApiResponse(responseCode = "400", description = "Invalid loan type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<LoanDto>> getLoansByType(
            @Parameter(description = "Loan type (LENT or BORROWED)", required = true) @PathVariable String type) {
        
        try {
            Loan.LoanType loanType = Loan.LoanType.valueOf(type.toUpperCase());
            List<Loan> loans = loanService.getLoansByType(loanType);
            List<LoanDto> loanDtos = loans.stream()
                    .map(loanMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(loanDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get loans by status",
            description = "Retrieve loans filtered by status (ACTIVE, PAID_OFF, OVERDUE, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by status"),
            @ApiResponse(responseCode = "400", description = "Invalid loan status"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanDto>> getLoansByStatus(
            @Parameter(description = "Loan status", required = true) @PathVariable String status) {
        
        try {
            Loan.LoanStatus loanStatus = Loan.LoanStatus.valueOf(status.toUpperCase());
            List<Loan> loans = loanService.getLoansByStatus(loanStatus);
            List<LoanDto> loanDtos = loans.stream()
                    .map(loanMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(loanDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get loans by account",
            description = "Retrieve loans for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LoanDto>> getLoansByAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable Long accountId) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Loan> loans = loanService.getLoansByAccount(account.get());
        List<LoanDto> loanDtos = loans.stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    @Operation(
            summary = "Search loans by person name",
            description = "Search loans by person name containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching loans"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<LoanDto>> searchLoansByPersonName(
            @Parameter(description = "Person name pattern to search for") @RequestParam String personName) {
        
        List<Loan> loans = loanService.searchLoansByPersonName(personName);
        List<LoanDto> loanDtos = loans.stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    @Operation(
            summary = "Get loan by ID",
            description = "Retrieve a specific loan by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LoanDto> getLoanById(
            @Parameter(description = "Loan ID", required = true) @PathVariable Long id) {
        
        Optional<Loan> loan = loanService.getLoanById(id);
        return loan.map(loanMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create a new loan",
            description = "Create a new loan in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<LoanDto> createLoan(@Valid @RequestBody LoanDto loanDto) {
        Loan loan = loanMapper.toEntity(loanDto);
        
        // Validate account exists if provided
        if (loanDto.getAccountId() != null) {
            Optional<Account> account = accountService.getAccountById(loanDto.getAccountId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            loan.setAccount(account.get());
        }
        
        Loan createdLoan = loanService.createLoan(loan);
        LoanDto createdLoanDto = loanMapper.toDto(createdLoan);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLoanDto);
    }

    @Operation(
            summary = "Update loan",
            description = "Update an existing loan"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<LoanDto> updateLoan(
            @Parameter(description = "Loan ID", required = true) @PathVariable Long id,
            @Valid @RequestBody LoanDto loanDto) {
        
        if (!loanService.getLoanById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        loanDto.setId(id);
        Loan loan = loanMapper.toEntity(loanDto);
        
        // Validate account exists if provided
        if (loanDto.getAccountId() != null) {
            Optional<Account> account = accountService.getAccountById(loanDto.getAccountId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            loan.setAccount(account.get());
        }
        
        Loan updatedLoan = loanService.updateLoan(loan);
        LoanDto updatedLoanDto = loanMapper.toDto(updatedLoan);
        return ResponseEntity.ok(updatedLoanDto);
    }

    @Operation(
            summary = "Delete loan",
            description = "Delete a loan from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Loan deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(
            @Parameter(description = "Loan ID", required = true) @PathVariable Long id) {
        
        try {
            loanService.deleteLoan(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== LOAN ACTIONS ==========

    @Operation(
            summary = "Record payment for loan",
            description = "Record a payment made for a specific loan"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment recorded successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/payment")
    public ResponseEntity<Loan> recordPayment(
            @Parameter(description = "Loan ID", required = true) @PathVariable Long id,
            @Parameter(description = "Payment amount") @RequestParam BigDecimal paymentAmount) {
        
        try {
            Loan loan = loanService.recordPayment(id, paymentAmount);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Mark loan as urgent",
            description = "Mark a loan as urgent"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as urgent successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/urgent")
    public ResponseEntity<Loan> markAsUrgent(
            @Parameter(description = "Loan ID", required = true) @PathVariable Long id) {
        
        try {
            Loan loan = loanService.markAsUrgent(id);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Mark loan as not urgent",
            description = "Mark a loan as not urgent"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as not urgent successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/not-urgent")
    public ResponseEntity<Loan> markAsNotUrgent(
            @Parameter(description = "Loan ID", required = true) @PathVariable Long id) {
        
        try {
            Loan loan = loanService.markAsNotUrgent(id);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== REPORTING ENDPOINTS ==========

    @Operation(
            summary = "Get loan summary report",
            description = "Get a comprehensive summary report of all loans including totals and counts"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loan summary report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/summary")
    public ResponseEntity<LoanService.LoanSummaryReport> getLoanSummaryReport() {
        LoanService.LoanSummaryReport report = loanService.getLoanSummaryReport();
        return ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Get overdue loans report",
            description = "Get a list of all overdue loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue loans report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/overdue")
    public ResponseEntity<List<Loan>> getOverdueLoansReport() {
        List<Loan> overdueLoans = loanService.getOverdueLoansReport();
        return ResponseEntity.ok(overdueLoans);
    }

    @Operation(
            summary = "Get loans due soon report",
            description = "Get a list of loans due within the specified number of days"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans due soon report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/due-soon")
    public ResponseEntity<List<Loan>> getLoansDueSoonReport(
            @Parameter(description = "Number of days ahead to check") @RequestParam(defaultValue = "7") int daysAhead) {
        
        List<Loan> loansDueSoon = loanService.getLoansDueSoonReport(daysAhead);
        return ResponseEntity.ok(loansDueSoon);
    }

    @Operation(
            summary = "Get urgent loans report",
            description = "Get a list of all urgent loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved urgent loans report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/urgent")
    public ResponseEntity<List<Loan>> getUrgentLoansReport() {
        List<Loan> urgentLoans = loanService.getUrgentLoans();
        return ResponseEntity.ok(urgentLoans);
    }

    @Operation(
            summary = "Get total amount lent",
            description = "Get the total amount of money lent to others"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total amount lent"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/total-lent")
    public ResponseEntity<BigDecimal> getTotalAmountLent() {
        BigDecimal totalLent = loanService.getTotalAmountLent();
        return ResponseEntity.ok(totalLent);
    }

    @Operation(
            summary = "Get total amount borrowed",
            description = "Get the total amount of money borrowed from others"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total amount borrowed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/total-borrowed")
    public ResponseEntity<BigDecimal> getTotalAmountBorrowed() {
        BigDecimal totalBorrowed = loanService.getTotalAmountBorrowed();
        return ResponseEntity.ok(totalBorrowed);
    }

    @Operation(
            summary = "Get net loan position",
            description = "Get the net loan position (total lent - total borrowed)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved net loan position"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/net-position")
    public ResponseEntity<BigDecimal> getNetLoanPosition() {
        BigDecimal netPosition = loanService.getNetLoanPosition();
        return ResponseEntity.ok(netPosition);
    }

    @Operation(
            summary = "Get loans by date range",
            description = "Get loans within a specific date range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/date-range")
    public ResponseEntity<List<Loan>> getLoansByDateRange(
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Loan> loans = loanService.getLoansByDateRange(startDate, endDate);
        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Get loans by due date range",
            description = "Get loans with due dates within a specific range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by due date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reports/due-date-range")
    public ResponseEntity<List<Loan>> getLoansByDueDateRange(
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Loan> loans = loanService.getLoansByDueDateRange(startDate, endDate);
        return ResponseEntity.ok(loans);
    }
}
