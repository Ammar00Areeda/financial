package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Dashboard API response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    
    private NetWorthDto netWorth;
    private List<AccountSummaryDto> accounts;
    private List<TransactionSummaryDto> recentTransactions;
    private List<LoanSummaryDto> activeLoans;
    private MonthlySpendingDto monthlySpending;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetWorthDto {
        private String currency;
        private BigDecimal current;
        private LocalDateTime lastUpdated;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummaryDto {
        private String id;
        private String name;
        private String type;
        private BigDecimal balance;
        private String currency;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionSummaryDto {
        private String id;
        private String date;
        private String accountId;
        private String accountName;
        private String categoryId;
        private String categoryName;
        private String description;
        private BigDecimal amount;
        private String type;
        private String currency;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanSummaryDto {
        private String id;
        private String name;
        private String direction;
        private String counterparty;
        private String accountId;
        private String accountName;
        private BigDecimal amount;
        private String currency;
        private String startDate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySpendingDto {
        private String month;
        private String currency;
        private List<SpendingBreakdownDto> breakdown;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendingBreakdownDto {
        private String label;
        private String categoryId;
        private BigDecimal amount;
    }
}
