package com.financial.controller;

import com.financial.dto.LoanCreateRequestDto;
import com.financial.dto.LoanDto;
import com.financial.dto.LoanInstallmentRequestDto;
import com.financial.dto.LoanInstallmentResponseDto;
import com.financial.dto.LoanListDTO;
import com.financial.dto.LoanResponseDto;
import com.financial.dto.LoanUpdateRequestDto;
import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.mapper.LoanMapper;
import com.financial.service.AccountService;
import com.financial.service.LoanService;
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
 * Implements LoanApi interface which contains all OpenAPI documentation.
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController implements LoanApi {

    private final LoanService loanService;
    private final AccountService accountService;
    private final LoanMapper loanMapper;

    // ========== BASIC CRUD OPERATIONS ==========

    @Override
    @GetMapping
    public ResponseEntity<Page<LoanListDTO>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "loanDate,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Loan> loans = loanService.getAllLoans(pageable);
        Page<LoanListDTO> loanDtos = loans.map(LoanListDTO::fromEntity);
        
        return ResponseEntity.ok(loanDtos);
    }

    @Override
    @GetMapping("/type/{type}")
    public ResponseEntity<List<LoanListDTO>> getLoansByType(@PathVariable String type) {
        
        try {
            Loan.LoanType loanType = Loan.LoanType.valueOf(type.toUpperCase());
            List<Loan> loans = loanService.getLoansByType(loanType);
            List<LoanListDTO> loanDtos = loans.stream()
                    .map(LoanListDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(loanDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanListDTO>> getLoansByStatus(@PathVariable String status) {
        
        try {
            Loan.LoanStatus loanStatus = Loan.LoanStatus.valueOf(status.toUpperCase());
            List<Loan> loans = loanService.getLoansByStatus(loanStatus);
            List<LoanListDTO> loanDtos = loans.stream()
                    .map(LoanListDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(loanDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LoanListDTO>> getLoansByAccount(@PathVariable Long accountId) {
        
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Loan> loans = loanService.getLoansByAccount(account.get());
        List<LoanListDTO> loanDtos = loans.stream()
                .map(LoanListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<List<LoanListDTO>> searchLoansByPersonName(@RequestParam String personName) {
        
        List<Loan> loans = loanService.searchLoansByPersonName(personName);
        List<LoanListDTO> loanDtos = loans.stream()
                .map(LoanListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    @Override
    @GetMapping("/summary")
    public ResponseEntity<LoanService.LoanSummaryReport> getLoanSummary() {
        LoanService.LoanSummaryReport report = loanService.getLoanSummaryReport();
        return ResponseEntity.ok(report);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<LoanDto> getLoanById(@PathVariable Long id) {
        
        Optional<Loan> loan = loanService.getLoanById(id);
        return loan.map(loanMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get loan by ID (returns detailed response DTO).
     *
     * @param id the loan ID
     * @return ResponseEntity containing the loan response DTO
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<LoanResponseDto> getLoanDetailsById(@PathVariable Long id) {
        
        Optional<Loan> loan = loanService.getLoanById(id);
        return loan.map(loanMapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping
    public ResponseEntity<LoanDto> createLoan(@Valid @RequestBody LoanDto loanDto) {
        Loan loan = loanMapper.toEntity(loanDto);
        
        // Validate account exists (accountId is now mandatory)
        Optional<Account> account = accountService.getAccountById(loanDto.getAccountId());
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        loan.setAccount(account.get());
        
        Loan createdLoan = loanService.createLoan(loan);
        LoanDto createdLoanDto = loanMapper.toDto(createdLoan);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLoanDto);
    }
    
    /**
     * Create a new loan (using request DTO).
     *
     * @param requestDto the loan creation request
     * @return ResponseEntity containing the created loan response DTO
     */
    @PostMapping("/v2")
    public ResponseEntity<LoanResponseDto> createLoanV2(@Valid @RequestBody LoanCreateRequestDto requestDto) {
        Loan loan = loanMapper.toEntityFromCreateRequest(requestDto);
        
        // Validate account exists (accountId is now mandatory)
        Optional<Account> account = accountService.getAccountById(requestDto.getAccountId());
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        loan.setAccount(account.get());
        
        Loan createdLoan = loanService.createLoan(loan);
        LoanResponseDto responseDto = loanMapper.toResponseDto(createdLoan);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<LoanDto> updateLoan(
            @PathVariable Long id,
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
    
    /**
     * Update an existing loan (using request DTO).
     *
     * @param id the loan ID
     * @param requestDto the loan update request
     * @return ResponseEntity containing the updated loan response DTO
     */
    @PutMapping("/{id}/v2")
    public ResponseEntity<LoanResponseDto> updateLoanV2(
            @PathVariable Long id,
            @Valid @RequestBody LoanUpdateRequestDto requestDto) {
        
        Optional<Loan> existingLoanOpt = loanService.getLoanById(id);
        if (existingLoanOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Loan existingLoan = existingLoanOpt.get();
        
        // Update entity from request DTO
        loanMapper.updateEntityFromUpdateRequest(existingLoan, requestDto);
        existingLoan.setId(id);
        
        // Validate account exists if provided
        if (requestDto.getAccountId() != null) {
            Optional<Account> account = accountService.getAccountById(requestDto.getAccountId());
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            existingLoan.setAccount(account.get());
        }
        
        Loan updatedLoan = loanService.updateLoan(existingLoan);
        LoanResponseDto responseDto = loanMapper.toResponseDto(updatedLoan);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        
        try {
            loanService.deleteLoan(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== LOAN ACTIONS ==========

    @Override
    @PostMapping("/{id}/payment")
    public ResponseEntity<Loan> recordPayment(
            @PathVariable Long id,
            @RequestParam BigDecimal paymentAmount) {
        
        try {
            Loan loan = loanService.recordPayment(id, paymentAmount);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{loanId}/installments")
    public ResponseEntity<LoanInstallmentResponseDto> recordInstallmentPayment(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanInstallmentRequestDto request) {
        
        try {
            LoanInstallmentResponseDto response = loanService.recordInstallmentPayment(loanId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/urgent")
    public ResponseEntity<Loan> markAsUrgent(@PathVariable Long id) {
        
        try {
            Loan loan = loanService.markAsUrgent(id);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/not-urgent")
    public ResponseEntity<Loan> markAsNotUrgent(@PathVariable Long id) {
        
        try {
            Loan loan = loanService.markAsNotUrgent(id);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== REPORTING ENDPOINTS ==========

    @Override
    @GetMapping("/reports/summary")
    public ResponseEntity<LoanService.LoanSummaryReport> getLoanSummaryReport() {
        LoanService.LoanSummaryReport report = loanService.getLoanSummaryReport();
        return ResponseEntity.ok(report);
    }

    @Override
    @GetMapping("/reports/overdue")
    public ResponseEntity<List<LoanListDTO>> getOverdueLoansReport() {
        List<Loan> overdueLoans = loanService.getOverdueLoansReport();
        List<LoanListDTO> loanDtos = overdueLoans.stream()
                .map(LoanListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    @Override
    @GetMapping("/reports/due-soon")
    public ResponseEntity<List<LoanListDTO>> getLoansDueSoonReport(@RequestParam(defaultValue = "7") int daysAhead) {
        
        List<Loan> loansDueSoon = loanService.getLoansDueSoonReport(daysAhead);
        List<LoanListDTO> loanDtos = loansDueSoon.stream()
                .map(LoanListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    @Override
    @GetMapping("/reports/urgent")
    public ResponseEntity<List<LoanListDTO>> getUrgentLoansReport() {
        List<Loan> urgentLoans = loanService.getUrgentLoans();
        List<LoanListDTO> loanDtos = urgentLoans.stream()
                .map(LoanListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    @Override
    @GetMapping("/reports/total-lent")
    public ResponseEntity<BigDecimal> getTotalAmountLent() {
        BigDecimal totalLent = loanService.getTotalAmountLent();
        return ResponseEntity.ok(totalLent);
    }

    @Override
    @GetMapping("/reports/total-borrowed")
    public ResponseEntity<BigDecimal> getTotalAmountBorrowed() {
        BigDecimal totalBorrowed = loanService.getTotalAmountBorrowed();
        return ResponseEntity.ok(totalBorrowed);
    }

    @Override
    @GetMapping("/reports/net-position")
    public ResponseEntity<BigDecimal> getNetLoanPosition() {
        BigDecimal netPosition = loanService.getNetLoanPosition();
        return ResponseEntity.ok(netPosition);
    }

    @Override
    @GetMapping("/reports/date-range")
    public ResponseEntity<List<LoanListDTO>> getLoansByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Loan> loans = loanService.getLoansByDateRange(startDate, endDate);
        List<LoanListDTO> loanDtos = loans.stream()
                .map(LoanListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    @Override
    @GetMapping("/reports/due-date-range")
    public ResponseEntity<List<LoanListDTO>> getLoansByDueDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Loan> loans = loanService.getLoansByDueDateRange(startDate, endDate);
        List<LoanListDTO> loanDtos = loans.stream()
                .map(LoanListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDtos);
    }

    // DEBUG ENDPOINT - Remove in production
    @GetMapping("/debug/all")
    public ResponseEntity<String> debugAllLoans() {
        com.financial.entity.User currentUser = com.financial.security.SecurityUtils.getAuthenticatedUser();
        List<Loan> allLoans = loanService.debugGetAllLoansWithoutUserFilter();
        
        StringBuilder debug = new StringBuilder();
        debug.append("Current authenticated user: ").append(currentUser.getUsername())
             .append(" (ID: ").append(currentUser.getId()).append(")\n\n");
        debug.append("All loans in database:\n");
        
        for (Loan loan : allLoans) {
            debug.append("Loan ID: ").append(loan.getId())
                 .append(", Person: ").append(loan.getPersonName())
                 .append(", User ID: ").append(loan.getUser() != null ? loan.getUser().getId() : "NULL")
                 .append(", User: ").append(loan.getUser() != null ? loan.getUser().getUsername() : "NULL")
                 .append("\n");
        }
        
        return ResponseEntity.ok(debug.toString());
    }
}
