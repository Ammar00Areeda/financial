package com.financial.service;

import com.financial.dto.NetWorthResponseDto;
import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.entity.User;
import com.financial.repository.AccountRepository;
import com.financial.repository.LoanRepository;
import com.financial.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for calculating and managing net worth calculations.
 * 
 * <p>This service provides comprehensive net worth calculations by combining account
 * balances with loan positions. It calculates the user's total financial position
 * including assets (account balances) and net loan position (money lent - money borrowed).</p>
 * 
 * <p><b>Net Worth Formula:</b> Net Worth = Total Account Balance + Net Loan Position
 * where Net Loan Position = (Money Lent to Others) - (Money Borrowed from Others)</p>
 * 
 * <p><b>Security:</b> All methods require authentication. Calculations are automatically
 * scoped to the authenticated user retrieved via {@link SecurityUtils#getAuthenticatedUser()}.</p>
 * 
 * @see NetWorthResponseDto
 * @see AccountService
 * @see LoanService
 * @see SecurityUtils
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NetWorthService {
    
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    
    /**
     * Calculates comprehensive net worth for the authenticated user.
     * 
     * <p>This method provides a complete financial overview including:
     * <ul>
     * <li>Total account balances from all active accounts included in balance calculations</li>
     * <li>Net loan position (money lent to others - money borrowed from others)</li>
     * <li>Detailed breakdowns by account type and loan type</li>
     * <li>Counts of active accounts and loans</li>
     * <li>Overdue loan information</li>
     * </ul></p>
     * 
     * <p><b>Security:</b> Requires authentication. Only includes data belonging to
     * the authenticated user.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * NetWorthResponseDto netWorth = netWorthService.calculateNetWorth();
     * 
     * System.out.println("Total Net Worth: $" + netWorth.getTotalNetWorth());
     * System.out.println("Account Balance: $" + netWorth.getTotalAccountBalance());
     * System.out.println("Net Loan Position: $" + netWorth.getNetLoanPosition());
     * 
     * if (netWorth.getNetLoanPosition().compareTo(BigDecimal.ZERO) > 0) {
     *     System.out.println("You are a net lender");
     * } else {
     *     System.out.println("You are a net borrower");
     * }
     * }</pre>
     *
     * @return comprehensive net worth response with all financial metrics
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public NetWorthResponseDto calculateNetWorth() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        log.debug("Calculating net worth for user: {}", currentUser.getUsername());
        
        // Get all active accounts included in balance calculation
        List<Account> accounts = accountRepository.findByUserAndIncludeInBalanceAndStatus(
            currentUser, true, Account.AccountStatus.ACTIVE);
        
        // Get all loans for the user
        List<Loan> allLoans = loanRepository.findByUser(currentUser.getId(), 
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        // Calculate total account balance
        BigDecimal totalAccountBalance = accounts.stream()
            .map(Account::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate loan metrics
        BigDecimal totalAmountLent = allLoans.stream()
            .filter(loan -> loan.getLoanType() == Loan.LoanType.LENT)
            .map(Loan::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAmountBorrowed = allLoans.stream()
            .filter(loan -> loan.getLoanType() == Loan.LoanType.BORROWED)
            .map(Loan::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netLoanPosition = totalAmountLent.subtract(totalAmountBorrowed);
        
        // Calculate total net worth
        BigDecimal totalNetWorth = totalAccountBalance.add(netLoanPosition);
        
        // Build account balances by type
        List<NetWorthResponseDto.AccountTypeBalance> accountBalancesByType = 
            buildAccountBalancesByType(accounts);
        
        // Build loan summary by type
        List<NetWorthResponseDto.LoanTypeSummary> loanSummaryByType = 
            buildLoanSummaryByType(allLoans);
        
        // Count active loans and overdue loans
        long activeLoansCount = allLoans.stream()
            .filter(loan -> loan.getStatus() == Loan.LoanStatus.ACTIVE)
            .count();
        
        long overdueLoansCount = allLoans.stream()
            .filter(loan -> loan.isOverdue())
            .count();
        
        return NetWorthResponseDto.builder()
            .totalNetWorth(totalNetWorth)
            .totalAccountBalance(totalAccountBalance)
            .netLoanPosition(netLoanPosition)
            .totalAmountLent(totalAmountLent)
            .totalAmountBorrowed(totalAmountBorrowed)
            .accountBalancesByType(accountBalancesByType)
            .loanSummaryByType(loanSummaryByType)
            .activeAccountsCount(accounts.size())
            .activeLoansCount((int) activeLoansCount)
            .overdueLoansCount((int) overdueLoansCount)
            .currency("JD") // Default currency
            .calculatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .build();
    }
    
    /**
     * Builds account balance breakdown by account type.
     * 
     * @param accounts list of accounts to analyze
     * @return list of account type balances
     */
    private List<NetWorthResponseDto.AccountTypeBalance> buildAccountBalancesByType(List<Account> accounts) {
        Map<Account.AccountType, List<Account>> accountsByType = accounts.stream()
            .collect(Collectors.groupingBy(Account::getType));
        
        return accountsByType.entrySet().stream()
            .map(entry -> {
                Account.AccountType type = entry.getKey();
                List<Account> accountsOfType = entry.getValue();
                
                BigDecimal totalBalance = accountsOfType.stream()
                    .map(Account::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return NetWorthResponseDto.AccountTypeBalance.builder()
                    .accountType(type.name())
                    .accountTypeDisplayName(type.getDisplayName())
                    .totalBalance(totalBalance)
                    .accountCount(accountsOfType.size())
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Builds loan summary breakdown by loan type.
     * 
     * @param loans list of loans to analyze
     * @return list of loan type summaries
     */
    private List<NetWorthResponseDto.LoanTypeSummary> buildLoanSummaryByType(List<Loan> loans) {
        Map<Loan.LoanType, List<Loan>> loansByType = loans.stream()
            .collect(Collectors.groupingBy(Loan::getLoanType));
        
        return loansByType.entrySet().stream()
            .map(entry -> {
                Loan.LoanType type = entry.getKey();
                List<Loan> loansOfType = entry.getValue();
                
                BigDecimal totalAmount = loansOfType.stream()
                    .map(Loan::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal totalPaid = loansOfType.stream()
                    .map(Loan::getPaidAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal remainingAmount = loansOfType.stream()
                    .map(Loan::getRemainingAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                long activeLoanCount = loansOfType.stream()
                    .filter(loan -> loan.getStatus() == Loan.LoanStatus.ACTIVE)
                    .count();
                
                long overdueLoanCount = loansOfType.stream()
                    .filter(Loan::isOverdue)
                    .count();
                
                return NetWorthResponseDto.LoanTypeSummary.builder()
                    .loanType(type.name())
                    .loanTypeDisplayName(type.getDisplayName())
                    .totalAmount(totalAmount)
                    .totalPaid(totalPaid)
                    .remainingAmount(remainingAmount)
                    .loanCount(loansOfType.size())
                    .activeLoanCount((int) activeLoanCount)
                    .overdueLoanCount((int) overdueLoanCount)
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Gets a simplified net worth calculation (just the total).
     * 
     * <p>This method provides a quick calculation of net worth without detailed
     * breakdowns. Useful for dashboard displays or quick checks.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only includes data belonging to
     * the authenticated user.</p>
     *
     * @return total net worth as a BigDecimal
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public BigDecimal getTotalNetWorth() {
        NetWorthResponseDto netWorth = calculateNetWorth();
        return netWorth.getTotalNetWorth();
    }
    
    /**
     * Gets net worth breakdown by account type only.
     * 
     * <p>This method provides account balance information without loan details.
     * Useful for analyzing asset distribution.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only includes data belonging to
     * the authenticated user.</p>
     *
     * @return list of account type balances
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public List<NetWorthResponseDto.AccountTypeBalance> getAccountBalancesByType() {
        NetWorthResponseDto netWorth = calculateNetWorth();
        return netWorth.getAccountBalancesByType();
    }
    
    /**
     * Gets loan summary by type only.
     * 
     * <p>This method provides loan information without account details.
     * Useful for analyzing lending and borrowing positions.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only includes data belonging to
     * the authenticated user.</p>
     *
     * @return list of loan type summaries
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public List<NetWorthResponseDto.LoanTypeSummary> getLoanSummaryByType() {
        NetWorthResponseDto netWorth = calculateNetWorth();
        return netWorth.getLoanSummaryByType();
    }
}
