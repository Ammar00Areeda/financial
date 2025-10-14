package com.financial.controller;

import com.financial.dto.RecurringExpenseCreateRequestDto;
import com.financial.dto.RecurringExpenseListDTO;
import com.financial.dto.RecurringExpenseResponseDto;
import com.financial.dto.RecurringExpenseUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * API interface for RecurringExpense management operations.
 * Contains all OpenAPI documentation for recurring expense endpoints.
 */
@Tag(name = "Recurring Expenses", description = "Recurring expense management operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface RecurringExpenseApi {

    @Operation(
            summary = "Get all recurring expenses",
            description = "Retrieve a list of all recurring expenses with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Page<RecurringExpenseListDTO>> getAllRecurringExpenses(
            @Parameter(description = "Page number (0-based)") int page,
            @Parameter(description = "Number of items per page") int size,
            @Parameter(description = "Sort criteria (field,direction)") String sort);

    @Operation(
            summary = "Get recurring expenses by account",
            description = "Retrieve recurring expenses for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses by account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesByAccount(
            @Parameter(description = "Account ID", required = true) Long accountId);

    @Operation(
            summary = "Get recurring expenses by status",
            description = "Retrieve recurring expenses filtered by status (ACTIVE, PAUSED, CANCELLED, COMPLETED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses by status"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesByStatus(
            @Parameter(description = "Status", required = true) String status);

    @Operation(
            summary = "Get recurring expenses by frequency",
            description = "Retrieve recurring expenses filtered by frequency (DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses by frequency"),
            @ApiResponse(responseCode = "400", description = "Invalid frequency"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesByFrequency(
            @Parameter(description = "Frequency", required = true) String frequency);

    @Operation(
            summary = "Get recurring expenses due today",
            description = "Retrieve recurring expenses that are due today"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses due today"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesDueToday();

    @Operation(
            summary = "Get overdue recurring expenses",
            description = "Retrieve recurring expenses that are overdue"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> getOverdueRecurringExpenses();

    @Operation(
            summary = "Get recurring expenses due soon",
            description = "Retrieve recurring expenses that are due within the specified number of days"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses due soon"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesDueSoon(
            @Parameter(description = "Number of days ahead to check") int daysAhead);

    @Operation(
            summary = "Get recurring expenses with auto-pay",
            description = "Retrieve recurring expenses that have auto-pay enabled"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recurring expenses with auto-pay"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> getRecurringExpensesWithAutoPay();

    @Operation(
            summary = "Search recurring expenses by name",
            description = "Search recurring expenses by name containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> searchRecurringExpensesByName(
            @Parameter(description = "Name pattern to search for") String name);

    @Operation(
            summary = "Search recurring expenses by provider",
            description = "Search recurring expenses by provider containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<RecurringExpenseListDTO>> searchRecurringExpensesByProvider(
            @Parameter(description = "Provider pattern to search for") String provider);

    @Operation(
            summary = "Get recurring expense by ID",
            description = "Retrieve a specific recurring expense by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense found"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<RecurringExpenseResponseDto> getRecurringExpenseById(
            @Parameter(description = "Recurring expense ID", required = true) Long id);

    @Operation(
            summary = "Create a new recurring expense",
            description = "Create a new recurring expense in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recurring expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Account or category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<RecurringExpenseResponseDto> createRecurringExpense(RecurringExpenseCreateRequestDto request);

    @Operation(
            summary = "Update recurring expense",
            description = "Update an existing recurring expense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<RecurringExpenseResponseDto> updateRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) Long id,
            RecurringExpenseUpdateRequestDto request);

    @Operation(
            summary = "Delete recurring expense",
            description = "Delete a recurring expense from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recurring expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Void> deleteRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) Long id);

    // ========== ACTION ENDPOINTS ==========

    @Operation(
            summary = "Mark recurring expense as paid",
            description = "Mark a recurring expense as paid and create a transaction record"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense marked as paid successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<RecurringExpenseResponseDto> markAsPaid(
            @Parameter(description = "Recurring expense ID", required = true) Long id);

    @Operation(
            summary = "Pause recurring expense",
            description = "Pause a recurring expense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense paused successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<RecurringExpenseResponseDto> pauseRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) Long id);

    @Operation(
            summary = "Resume recurring expense",
            description = "Resume a paused recurring expense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense resumed successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<RecurringExpenseResponseDto> resumeRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) Long id);

    @Operation(
            summary = "Cancel recurring expense",
            description = "Cancel a recurring expense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring expense cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Recurring expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<RecurringExpenseResponseDto> cancelRecurringExpense(
            @Parameter(description = "Recurring expense ID", required = true) Long id);

    // ========== REPORTING ENDPOINTS ==========

    @Operation(
            summary = "Get total monthly recurring expenses for account",
            description = "Calculate total monthly recurring expenses for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total monthly recurring expenses"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalMonthlyRecurringExpenses(
            @Parameter(description = "Account ID", required = true) Long accountId);

    @Operation(
            summary = "Get total recurring expenses for account",
            description = "Calculate total recurring expenses for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total recurring expenses"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalRecurringExpenses(
            @Parameter(description = "Account ID", required = true) Long accountId);

    @Operation(
            summary = "Process all due recurring expenses",
            description = "Process all recurring expenses that are due today (for scheduled jobs)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed due recurring expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Integer> processAllDueRecurringExpenses();
}

