package com.financial.controller;

import com.financial.dto.ReportsDto;
import com.financial.entity.Transaction;
import com.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for Reports operations.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Financial reports and analytics operations")
public class ReportsController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Get monthly reports",
            description = "Retrieve monthly financial reports for the specified date range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly reports"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/monthly")
    public ResponseEntity<ReportsDto> getMonthlyReports(
            @Parameter(description = "Start date in YYYY-MM format") @RequestParam String start,
            @Parameter(description = "End date in YYYY-MM format") @RequestParam String end) {
        
        // Parse date range
        LocalDate startDate = LocalDate.parse(start + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = LocalDate.parse(end + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Generate monthly reports (simplified implementation)
        List<ReportsDto.MonthlyReportDto> reports = generateMonthlyReports(startDate, endDate);
        
        // Calculate totals
        BigDecimal totalIncome = reports.stream()
                .map(ReportsDto.MonthlyReportDto::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = reports.stream()
                .map(ReportsDto.MonthlyReportDto::getExpenses)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSavings = reports.stream()
                .map(ReportsDto.MonthlyReportDto::getSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        ReportsDto response = ReportsDto.builder()
                .reports(reports)
                .totals(ReportsDto.TotalsDto.builder()
                        .income(totalIncome)
                        .expenses(totalExpenses)
                        .savings(totalSavings)
                        .currency("USD")
                        .build())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    private List<ReportsDto.MonthlyReportDto> generateMonthlyReports(LocalDate startDate, LocalDate endDate) {
        List<ReportsDto.MonthlyReportDto> reports = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String monthId = "report-" + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String month = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            // Calculate month boundaries
            LocalDateTime monthStart = currentDate.atStartOfDay();
            LocalDateTime monthEnd = currentDate.plusMonths(1).atStartOfDay().minusSeconds(1);
            
            // Get all transactions for the month
            List<Transaction> monthTransactions = transactionService.getTransactionsByDateRange(monthStart, monthEnd);
            
            // Calculate income and expenses
            BigDecimal income = monthTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal expenses = monthTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal savings = income.subtract(expenses);
            
            ReportsDto.MonthlyReportDto report = ReportsDto.MonthlyReportDto.builder()
                    .id(monthId)
                    .month(month)
                    .income(income)
                    .expenses(expenses)
                    .savings(savings)
                    .currency("USD")
                    .createdAt(LocalDateTime.now())
                    .build();
            
            reports.add(report);
            currentDate = currentDate.plusMonths(1);
        }
        
        return reports;
    }
}
