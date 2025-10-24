package com.financial.api;

import com.financial.dto.LoanDto;
import com.financial.dto.LoanListDTO;
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
@Tag(name = "Loans", description = "Loan management operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface LoanApi {

    @Operation(
            summary = "Get all loans with pagination",
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
            description = "Retrieve loans filtered by type (LENT, BORROWED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by type"),
            @ApiResponse(responseCode = "400", description = "Invalid loan type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansByType(
            @Parameter(description = "Loan type", required = true) String type);

    @Operation(
            summary = "Get loans by status",
            description = "Retrieve loans filtered by status (ACTIVE, PAID, OVERDUE)"
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
            description = "Retrieve loans associated with a specific account"
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
            @Parameter(description = "Person name pattern to search for") String name);

    @Operation(
            summary = "Get loan summary",
            description = "Get summary statistics for all loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loan summary"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanDto> getLoanSummary();

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
    ResponseEntity<LoanDto> createLoan(
            @Parameter(description = "Loan creation request") LoanDto request);

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
            @Parameter(description = "Loan update request") LoanDto request);

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

    @Operation(
            summary = "Record loan payment",
            description = "Record a payment against a loan"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment recorded successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanDto> recordPayment(
            @Parameter(description = "Loan ID", required = true) Long id,
            @Parameter(description = "Payment amount") BigDecimal amount);

    @Operation(
            summary = "Mark loan as urgent",
            description = "Mark a loan as urgent"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as urgent successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanDto> markAsUrgent(
            @Parameter(description = "Loan ID", required = true) Long id);

    @Operation(
            summary = "Mark loan as not urgent",
            description = "Remove urgent status from a loan"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as not urgent successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanDto> markAsNotUrgent(
            @Parameter(description = "Loan ID", required = true) Long id);

    @Operation(
            summary = "Mark loan as paid",
            description = "Mark a loan as fully paid and update its status to PAID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as paid successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "400", description = "Bad request - loan may already be paid"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanDto> markLoanAsPaid(
            @Parameter(description = "Loan ID", required = true) Long id);

    @Operation(
            summary = "Get loan summary report",
            description = "Get detailed summary report for all loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loan summary report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<LoanDto> getLoanSummaryReport();

    @Operation(
            summary = "Get overdue loans report",
            description = "Get report of all overdue loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue loans report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getOverdueLoansReport();

    @Operation(
            summary = "Get loans due soon report",
            description = "Get report of loans due within specified days"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans due soon report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansDueSoonReport(
            @Parameter(description = "Number of days to look ahead") int days);

    @Operation(
            summary = "Get urgent loans report",
            description = "Get report of all urgent loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved urgent loans report"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getUrgentLoansReport();

    @Operation(
            summary = "Get total amount lent",
            description = "Calculate total amount of money lent to others"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total amount lent"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalAmountLent();

    @Operation(
            summary = "Get total amount borrowed",
            description = "Calculate total amount of money borrowed from others"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total amount borrowed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalAmountBorrowed();

    @Operation(
            summary = "Get net loan position",
            description = "Calculate net loan position (amount lent - amount borrowed)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated net loan position"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getNetLoanPosition();

    @Operation(
            summary = "Get loans by date range",
            description = "Retrieve loans created within a specific date range"
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
            description = "Retrieve loans with due dates within a specific range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans by due date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<LoanListDTO>> getLoansByDueDateRange(
            @Parameter(description = "Start due date (yyyy-MM-ddTHH:mm:ss)") LocalDateTime startDate,
            @Parameter(description = "End due date (yyyy-MM-ddTHH:mm:ss)") LocalDateTime endDate);
}