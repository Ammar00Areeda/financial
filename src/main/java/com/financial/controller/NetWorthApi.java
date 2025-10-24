package com.financial.controller;

import com.financial.dto.NetWorthResponseDto;
import com.financial.service.NetWorthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for net worth calculations and financial overview.
 * 
 * <p>This controller provides endpoints for calculating and retrieving comprehensive
 * net worth information including account balances, loan positions, and detailed
 * breakdowns by type. All endpoints require authentication and are scoped to the
 * authenticated user.</p>
 * 
 * <p><b>Net Worth Calculation:</b> Net Worth = Total Account Balance + Net Loan Position
 * where Net Loan Position = (Money Lent to Others) - (Money Borrowed from Others)</p>
 * 
 * <p><b>Security:</b> All endpoints require authentication. Data is automatically
 * scoped to the authenticated user.</p>
 * 
 * @see NetWorthService
 * @see NetWorthResponseDto
 */
@RestController
@RequestMapping("/api/v1/net-worth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Net Worth", description = "Net worth calculations and financial overview")
public class NetWorthApi {
    
    private final NetWorthService netWorthService;
    
    /**
     * Calculates and returns comprehensive net worth information for the authenticated user.
     * 
     * <p>This endpoint provides a complete financial overview including:
     * <ul>
     * <li>Total net worth (account balance + net loan position)</li>
     * <li>Total account balance from all active accounts</li>
     * <li>Net loan position (money lent - money borrowed)</li>
     * <li>Detailed breakdowns by account type and loan type</li>
     * <li>Counts of active accounts and loans</li>
     * <li>Overdue loan information</li>
     * </ul></p>
     * 
     * <p><b>Security:</b> Requires authentication. Only returns data belonging to
     * the authenticated user.</p>
     * 
     * <p><b>Example Response:</b></p>
     * <pre>{@code
     * {
     *   "totalNetWorth": 15000.00,
     *   "totalAccountBalance": 12000.00,
     *   "netLoanPosition": 3000.00,
     *   "totalAmountLent": 5000.00,
     *   "totalAmountBorrowed": 2000.00,
     *   "accountBalancesByType": [
     *     {
     *       "accountType": "SAVINGS",
     *       "accountTypeDisplayName": "Savings",
     *       "totalBalance": 8000.00,
     *       "accountCount": 2
     *     }
     *   ],
     *   "loanSummaryByType": [
     *     {
     *       "loanType": "LENT",
     *       "loanTypeDisplayName": "Lent Money",
     *       "totalAmount": 5000.00,
     *       "totalPaid": 2000.00,
     *       "remainingAmount": 3000.00,
     *       "loanCount": 3,
     *       "activeLoanCount": 2,
     *       "overdueLoanCount": 0
     *     }
     *   ],
     *   "activeAccountsCount": 3,
     *   "activeLoansCount": 2,
     *   "overdueLoansCount": 0,
     *   "currency": "JD",
     *   "calculatedAt": "2024-01-15T10:30:00"
     * }
     * }</pre>
     *
     * @return ResponseEntity containing comprehensive net worth information
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Get comprehensive net worth information",
        description = "Calculates and returns complete net worth information including account balances, " +
                    "loan positions, and detailed breakdowns by type for the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Net worth information retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NetWorthResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        )
    })
    public ResponseEntity<NetWorthResponseDto> getNetWorth() {
        log.debug("Calculating net worth for authenticated user");
        
        NetWorthResponseDto netWorth = netWorthService.calculateNetWorth();
        
        log.debug("Net worth calculated: Total={}, Account Balance={}, Net Loan Position={}", 
            netWorth.getTotalNetWorth(), 
            netWorth.getTotalAccountBalance(), 
            netWorth.getNetLoanPosition());
        
        return ResponseEntity.ok(netWorth);
    }
    
    /**
     * Gets the total net worth as a simple number for the authenticated user.
     * 
     * <p>This endpoint provides a quick calculation of total net worth without detailed
     * breakdowns. Useful for dashboard displays or quick financial checks.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only returns data belonging to
     * the authenticated user.</p>
     * 
     * <p><b>Example Response:</b></p>
     * <pre>{@code
     * 15000.00
     * }</pre>
     *
     * @return ResponseEntity containing the total net worth as a BigDecimal
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    @GetMapping("/total")
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Get total net worth",
        description = "Returns the total net worth as a simple number without detailed breakdowns."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Total net worth retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "number", format = "decimal")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        )
    })
    public ResponseEntity<BigDecimal> getTotalNetWorth() {
        log.debug("Getting total net worth for authenticated user");
        
        BigDecimal totalNetWorth = netWorthService.getTotalNetWorth();
        
        log.debug("Total net worth: {}", totalNetWorth);
        
        return ResponseEntity.ok(totalNetWorth);
    }
    
    /**
     * Gets account balance breakdown by type for the authenticated user.
     * 
     * <p>This endpoint provides account balance information grouped by account type
     * without loan details. Useful for analyzing asset distribution across different
     * account types.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only returns data belonging to
     * the authenticated user.</p>
     * 
     * <p><b>Example Response:</b></p>
     * <pre>{@code
     * [
     *   {
     *     "accountType": "SAVINGS",
     *     "accountTypeDisplayName": "Savings",
     *     "totalBalance": 8000.00,
     *     "accountCount": 2
     *   },
     *   {
     *     "accountType": "BANK_ACCOUNT",
     *     "accountTypeDisplayName": "Bank Account",
     *     "totalBalance": 4000.00,
     *     "accountCount": 1
     *   }
     * ]
     * }</pre>
     *
     * @return ResponseEntity containing account balances by type
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Get account balances by type",
        description = "Returns account balance breakdown by account type without loan information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Account balances retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NetWorthResponseDto.AccountTypeBalance.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        )
    })
    public ResponseEntity<List<NetWorthResponseDto.AccountTypeBalance>> getAccountBalancesByType() {
        log.debug("Getting account balances by type for authenticated user");
        
        List<NetWorthResponseDto.AccountTypeBalance> accountBalances = 
            netWorthService.getAccountBalancesByType();
        
        log.debug("Retrieved {} account type balances", accountBalances.size());
        
        return ResponseEntity.ok(accountBalances);
    }
    
    /**
     * Gets loan summary breakdown by type for the authenticated user.
     * 
     * <p>This endpoint provides loan information grouped by loan type without account
     * details. Useful for analyzing lending and borrowing positions.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only returns data belonging to
     * the authenticated user.</p>
     * 
     * <p><b>Example Response:</b></p>
     * <pre>{@code
     * [
     *   {
     *     "loanType": "LENT",
     *     "loanTypeDisplayName": "Lent Money",
     *     "totalAmount": 5000.00,
     *     "totalPaid": 2000.00,
     *     "remainingAmount": 3000.00,
     *     "loanCount": 3,
     *     "activeLoanCount": 2,
     *     "overdueLoanCount": 0
     *   },
     *   {
     *     "loanType": "BORROWED",
     *     "loanTypeDisplayName": "Borrowed Money",
     *     "totalAmount": 2000.00,
     *     "totalPaid": 500.00,
     *     "remainingAmount": 1500.00,
     *     "loanCount": 1,
     *     "activeLoanCount": 1,
     *     "overdueLoanCount": 0
     *   }
     * ]
     * }</pre>
     *
     * @return ResponseEntity containing loan summary by type
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    @GetMapping("/loans")
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Get loan summary by type",
        description = "Returns loan summary breakdown by loan type without account information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Loan summary retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NetWorthResponseDto.LoanTypeSummary.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        )
    })
    public ResponseEntity<List<NetWorthResponseDto.LoanTypeSummary>> getLoanSummaryByType() {
        log.debug("Getting loan summary by type for authenticated user");
        
        List<NetWorthResponseDto.LoanTypeSummary> loanSummary = 
            netWorthService.getLoanSummaryByType();
        
        log.debug("Retrieved {} loan type summaries", loanSummary.size());
        
        return ResponseEntity.ok(loanSummary);
    }
}
