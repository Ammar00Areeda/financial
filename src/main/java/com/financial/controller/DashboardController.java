package com.financial.controller;

import com.financial.dto.DashboardDto;
import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.entity.Transaction;
import com.financial.service.AccountService;
import com.financial.service.LoanService;
import com.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Dashboard operations.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard data and overview operations")
public class DashboardController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;

    @Operation(
            summary = "Get dashboard data",
            description = "Retrieve comprehensive dashboard data including net worth, accounts, recent transactions, active loans, and monthly spending"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard() {
        
        // Get all accounts
        List<Account> accounts = accountService.getAllAccounts();
        
        // Calculate net worth (sum of all account balances)
        BigDecimal netWorth = accounts.stream()
                .filter(Account::getIncludeInBalance)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get recent transactions (last 10)
        Pageable recentTransactionsPage = PageRequest.of(0, 10);
        List<Transaction> recentTransactions = transactionService.getAllTransactions(recentTransactionsPage).getContent();
        
        // Get active loans
        List<Loan> activeLoans = loanService.getLoansByStatus(Loan.LoanStatus.ACTIVE);
        
        // Build dashboard response
        DashboardDto dashboard = DashboardDto.builder()
                .netWorth(DashboardDto.NetWorthDto.builder()
                        .currency("JOD") // Default currency
                        .current(netWorth)
                        .lastUpdated(LocalDateTime.now())
                        .build())
                .accounts(accounts.stream()
                        .map(account -> DashboardDto.AccountSummaryDto.builder()
                                .id(account.getId().toString())
                                .name(account.getName())
                                .type(account.getType().toString().toLowerCase())
                                .balance(account.getBalance())
                                .currency(account.getCurrency())
                                .build())
                        .collect(Collectors.toList()))
                .recentTransactions(recentTransactions.stream()
                        .map(transaction -> DashboardDto.TransactionSummaryDto.builder()
                                .id(transaction.getId().toString())
                                .date(transaction.getTransactionDate().toLocalDate().toString())
                                .accountId(transaction.getAccount().getId().toString())
                                .accountName(transaction.getAccount().getName())
                                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId().toString() : null)
                                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                                .description(transaction.getDescription())
                                .amount(transaction.getAmount())
                                .type(transaction.getType().toString().toLowerCase())
                                .currency("USD") // Default currency
                                .build())
                        .collect(Collectors.toList()))
                .activeLoans(activeLoans.stream()
                        .map(loan -> DashboardDto.LoanSummaryDto.builder()
                                .id(loan.getId().toString())
                                .name(loan.getDescription() != null ? loan.getDescription() : "Loan to " + loan.getPersonName())
                                .direction(loan.getLoanType() == Loan.LoanType.LENT ? "given" : "received")
                                .counterparty(loan.getPersonName())
                                .accountId(loan.getAccount() != null ? loan.getAccount().getId().toString() : null)
                                .accountName(loan.getAccount() != null ? loan.getAccount().getName() : null)
                                .amount(loan.getTotalAmount() != null ? loan.getTotalAmount() : loan.getPrincipalAmount())
                                .currency("USD") // Default currency
                                .startDate(loan.getLoanDate().toLocalDate().toString())
                                .build())
                        .collect(Collectors.toList()))
                .monthlySpending(DashboardDto.MonthlySpendingDto.builder()
                        .month(LocalDateTime.now().toLocalDate().withDayOfMonth(1).toString())
                        .currency("USD")
                        .breakdown(getMonthlySpendingBreakdown())
                        .build())
                .build();
        
        return ResponseEntity.ok(dashboard);
    }
    
    private List<DashboardDto.SpendingBreakdownDto> getMonthlySpendingBreakdown() {
        // This is a simplified implementation
        // In a real application, you would query the database for actual spending by category
        return List.of(
                DashboardDto.SpendingBreakdownDto.builder()
                        .label("Groceries")
                        .categoryId("cat-2")
                        .amount(new BigDecimal("210.45"))
                        .build(),
                DashboardDto.SpendingBreakdownDto.builder()
                        .label("Utilities")
                        .categoryId("cat-3")
                        .amount(new BigDecimal("120.50"))
                        .build(),
                DashboardDto.SpendingBreakdownDto.builder()
                        .label("Loans Given")
                        .amount(new BigDecimal("1500.00"))
                        .build()
        );
    }
}
