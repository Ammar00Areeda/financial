package com.financial.controller;

import com.financial.dto.LoanDto;
import com.financial.dto.LoanListDTO;
import com.financial.entity.Loan;
import com.financial.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * API interface for Loan management operations.
 * Contains all OpenAPI documentation for loan endpoints.
 */
@Tag(name = "Loans", description = "Loan management and reporting operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface LoanApi {

    // ========== BASIC CRUD OPERATIONS ==========

    @Operation(
            summary = "Get all loans",
            description = "Retrieve a list of all loans with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Page<LoanListDTO>> getAllLoans(
            @Parameter(description = "Page number (0-based)") int page,
            @Parameter(description = "Number of items per page") int size,
            @Parameter(description = "Sort criteria (field,direction)") String sort);

    @Operation(
            summary = "Get loans by type",
            description = "Retrieve loans filtered by type (LENT or BORROWED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by type"),
            @ApiResponse(responseCode = "400", description = "Invalid loan type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansByType(
            @Parameter(description = "Loan type (LENT or BORROWED)", required = true) String type);

    @Operation(
            summary = "Get loans by status",
            description = "Retrieve loans filtered by status (ACTIVE, PAID_OFF, OVERDUE, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by status"),
            @ApiResponse(responseCode = "400", description = "Invalid loan status"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansByStatus(
            @Parameter(description = "Loan status", required = true) String status);

    @Operation(
            summary = "Get loans by account",
            description = "Retrieve loans for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansByAccount(
            @Parameter(description = "Account ID", required = true) Long accountId);

    @Operation(
            summary = "Search loans by person name",
            description = "Search loans by person name containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching loans"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> searchLoansByPersonName(
            @Parameter(description = "Person name pattern to search for") String personName);

    @Operation(
            summary = "Get loan summary",
            description = "Get a comprehensive summary report of all loans including totals and counts"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loan summary"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanService.LoanSummaryReport> getLoanSummary();

    @Operation(
            summary = "Get loan by ID",
            description = "Retrieve a specific loan by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanDto> getLoanById(
            @Parameter(description = "Loan ID", required = true) Long id);

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
    ResponseEntity<LoanDto> createLoan(LoanDto loanDto);

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
    ResponseEntity<LoanDto> updateLoan(
            @Parameter(description = "Loan ID", required = true) Long id,
            LoanDto loanDto);

    @Operation(
            summary = "Delete loan",
            description = "Delete a loan from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Loan deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Void> deleteLoan(
            @Parameter(description = "Loan ID", required = true) Long id);

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
    ResponseEntity<Loan> recordPayment(
            @Parameter(description = "Loan ID", required = true) Long id,
            @Parameter(description = "Payment amount") BigDecimal paymentAmount);

    @Operation(
            summary = "Mark loan as urgent",
            description = "Mark a loan as urgent"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as urgent successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Loan> markAsUrgent(
            @Parameter(description = "Loan ID", required = true) Long id);

    @Operation(
            summary = "Mark loan as not urgent",
            description = "Mark a loan as not urgent"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as not urgent successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Loan> markAsNotUrgent(
            @Parameter(description = "Loan ID", required = true) Long id);

    // ========== REPORTING ENDPOINTS ==========

    @Operation(
            summary = "Get loan summary report",
            description = "Get a comprehensive summary report of all loans including totals and counts"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loan summary report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanService.LoanSummaryReport> getLoanSummaryReport();

    @Operation(
            summary = "Get overdue loans report",
            description = "Get a list of all overdue loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue loans report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getOverdueLoansReport();

    @Operation(
            summary = "Get loans due soon report",
            description = "Get a list of loans due within the specified number of days"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans due soon report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansDueSoonReport(
            @Parameter(description = "Number of days ahead to check") int daysAhead);

    @Operation(
            summary = "Get urgent loans report",
            description = "Get a list of all urgent loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved urgent loans report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getUrgentLoansReport();

    @Operation(
            summary = "Get total amount lent",
            description = "Get the total amount of money lent to others"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total amount lent"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalAmountLent();

    @Operation(
            summary = "Get total amount borrowed",
            description = "Get the total amount of money borrowed from others"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total amount borrowed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalAmountBorrowed();

    @Operation(
            summary = "Get net loan position",
            description = "Get the net loan position (total lent - total borrowed)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved net loan position"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getNetLoanPosition();

    @Operation(
            summary = "Get loans by date range",
            description = "Get loans within a specific date range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansByDateRange(
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") LocalDateTime endDate);

    @Operation(
            summary = "Get loans by due date range",
            description = "Get loans with due dates within a specific range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by due date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansByDueDateRange(
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") LocalDateTime endDate);
}

