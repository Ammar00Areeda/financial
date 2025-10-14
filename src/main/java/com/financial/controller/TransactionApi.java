package com.financial.controller;

import com.financial.dto.TransactionCreateRequestDto;
import com.financial.dto.TransactionDto;
import com.financial.dto.TransactionListDTO;
import com.financial.dto.TransactionUpdateRequestDto;
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
 * API interface for Transaction management operations.
 * Contains all OpenAPI documentation for transaction endpoints.
 */
@Tag(name = "Transactions", description = "Transaction management operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface TransactionApi {

    @Operation(
            summary = "Get last 5 transactions",
            description = "Retrieve the last 5 transactions ordered by date descending"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved last 5 transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> getLast5Transactions();

    @Operation(
            summary = "Get last N transactions",
            description = "Retrieve the last N transactions ordered by date descending"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved last N transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> getLastTransactions(
            @Parameter(description = "Number of transactions to retrieve") int limit);

    @Operation(
            summary = "Get all transactions",
            description = "Retrieve a list of all transactions with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Page<TransactionListDTO>> getAllTransactions(
            @Parameter(description = "Page number (0-based)") int page,
            @Parameter(description = "Number of items per page") int size,
            @Parameter(description = "Sort criteria (field,direction)") String sort);

    @Operation(
            summary = "Get transactions by account",
            description = "Retrieve transactions for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> getTransactionsByAccount(
            @Parameter(description = "Account ID", required = true) Long accountId);

    @Operation(
            summary = "Get transactions by account with pagination",
            description = "Retrieve transactions for a specific account with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Page<TransactionListDTO>> getTransactionsByAccountPaginated(
            @Parameter(description = "Account ID", required = true) Long accountId,
            @Parameter(description = "Page number (0-based)") int page,
            @Parameter(description = "Number of items per page") int size,
            @Parameter(description = "Sort criteria (field,direction)") String sort);

    @Operation(
            summary = "Get last N transactions by account",
            description = "Retrieve the last N transactions for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved last N transactions by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> getLastTransactionsByAccount(
            @Parameter(description = "Account ID", required = true) Long accountId,
            @Parameter(description = "Number of transactions to retrieve") int limit);

    @Operation(
            summary = "Get transactions by type",
            description = "Retrieve transactions filtered by type (INCOME, EXPENSE, TRANSFER)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by type"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> getTransactionsByType(
            @Parameter(description = "Transaction type", required = true) String type);

    @Operation(
            summary = "Get transactions by category",
            description = "Retrieve transactions filtered by category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by category"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> getTransactionsByCategory(
            @Parameter(description = "Category ID", required = true) Long categoryId);

    @Operation(
            summary = "Get transactions by date range",
            description = "Retrieve transactions within a specific date range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> getTransactionsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") LocalDateTime endDate);

    @Operation(
            summary = "Get transactions by amount range",
            description = "Retrieve transactions within a specific amount range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions by amount range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> getTransactionsByAmountRange(
            @Parameter(description = "Minimum amount") BigDecimal minAmount,
            @Parameter(description = "Maximum amount") BigDecimal maxAmount);

    @Operation(
            summary = "Search transactions by description",
            description = "Search transactions by description containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<TransactionListDTO>> searchTransactionsByDescription(
            @Parameter(description = "Description pattern to search for") String description);

    @Operation(
            summary = "Get transaction by ID",
            description = "Retrieve a specific transaction by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<TransactionDto> getTransactionById(
            @Parameter(description = "Transaction ID", required = true) Long id);

    @Operation(
            summary = "Create a new transaction",
            description = "Create a new transaction in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Account or category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<TransactionDto> createTransaction(TransactionCreateRequestDto request);

    @Operation(
            summary = "Update transaction",
            description = "Update an existing transaction"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<TransactionDto> updateTransaction(
            @Parameter(description = "Transaction ID", required = true) Long id,
            TransactionUpdateRequestDto request);

    @Operation(
            summary = "Delete transaction",
            description = "Delete a transaction from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Transaction ID", required = true) Long id);

    @Operation(
            summary = "Get total amount by type",
            description = "Calculate total amount for a specific transaction type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total amount by type"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalAmountByType(
            @Parameter(description = "Transaction type", required = true) String type);

    @Operation(
            summary = "Get total income for account",
            description = "Calculate total income amount for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total income"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalIncomeByAccount(
            @Parameter(description = "Account ID", required = true) Long accountId);

    @Operation(
            summary = "Get total expense for account",
            description = "Calculate total expense amount for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total expense"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalExpenseByAccount(
            @Parameter(description = "Account ID", required = true) Long accountId);
}

