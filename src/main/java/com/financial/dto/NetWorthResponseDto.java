package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for net worth response containing comprehensive financial overview.
 * 
 * <p>This DTO provides a complete picture of the user's financial position including
 * total account balances, loan positions, and calculated net worth. It includes both
 * summary totals and detailed breakdowns by account type and loan type.</p>
 * 
 * <p><b>Net Worth Calculation:</b> Net Worth = Total Account Balance + Net Loan Position
 * where Net Loan Position = (Money Lent to Others) - (Money Borrowed from Others)</p>
 * 
 * @see com.financial.service.NetWorthService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetWorthResponseDto {
    
    /**
     * Total net worth calculated as: Account Balance + Net Loan Position
     */
    private BigDecimal totalNetWorth;
    
    /**
     * Total balance across all accounts that are included in balance calculations
     */
    private BigDecimal totalAccountBalance;
    
    /**
     * Net loan position (money lent to others - money borrowed from others)
     * Positive value means you are a net lender, negative means you are a net borrower
     */
    private BigDecimal netLoanPosition;
    
    /**
     * Total amount of money lent to others (outstanding)
     */
    private BigDecimal totalAmountLent;
    
    /**
     * Total amount of money borrowed from others (outstanding)
     */
    private BigDecimal totalAmountBorrowed;
    
    /**
     * Breakdown of account balances by account type
     */
    private List<AccountTypeBalance> accountBalancesByType;
    
    /**
     * Breakdown of loan amounts by loan type
     */
    private List<LoanTypeSummary> loanSummaryByType;
    
    /**
     * Number of active accounts included in balance calculation
     */
    private Integer activeAccountsCount;
    
    /**
     * Number of active loans (both lent and borrowed)
     */
    private Integer activeLoansCount;
    
    /**
     * Number of overdue loans
     */
    private Integer overdueLoansCount;
    
    /**
     * Currency used for all amounts (defaults to JD)
     */
    @Builder.Default
    private String currency = "JD";
    
    /**
     * Timestamp when the net worth was calculated
     */
    private String calculatedAt;
    
    /**
     * Inner class representing account balance by type.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountTypeBalance {
        private String accountType;
        private String accountTypeDisplayName;
        private BigDecimal totalBalance;
        private Integer accountCount;
    }
    
    /**
     * Inner class representing loan summary by type.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanTypeSummary {
        private String loanType;
        private String loanTypeDisplayName;
        private BigDecimal totalAmount;
        private BigDecimal totalPaid;
        private BigDecimal remainingAmount;
        private Integer loanCount;
        private Integer activeLoanCount;
        private Integer overdueLoanCount;
    }
}
