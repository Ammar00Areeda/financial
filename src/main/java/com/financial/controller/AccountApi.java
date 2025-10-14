package com.financial.controller;

import com.financial.dto.AccountCreateRequestDto;
import com.financial.dto.AccountDto;
import com.financial.dto.AccountListDTO;
import com.financial.dto.AccountUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * API interface for Account management operations.
 * Contains all OpenAPI documentation for account endpoints.
 */
@Tag(name = "Accounts", description = "Account management operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface AccountApi {

    @Operation(
            summary = "Get all accounts",
            description = "Retrieve a list of all accounts"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<AccountListDTO>> getAllAccounts();

    @Operation(
            summary = "Get all active accounts",
            description = "Retrieve a list of all active accounts"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<AccountListDTO>> getAllActiveAccounts();

    @Operation(
            summary = "Get accounts by type",
            description = "Retrieve accounts filtered by type (WALLET, BANK_ACCOUNT, SAVINGS, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts by type"),
            @ApiResponse(responseCode = "400", description = "Invalid account type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<AccountListDTO>> getAccountsByType(
            @Parameter(description = "Account type", required = true) String type);

    @Operation(
            summary = "Get active accounts by type",
            description = "Retrieve active accounts filtered by type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active accounts by type"),
            @ApiResponse(responseCode = "400", description = "Invalid account type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<AccountListDTO>> getActiveAccountsByType(
            @Parameter(description = "Account type", required = true) String type);

    @Operation(
            summary = "Get accounts included in balance",
            description = "Retrieve accounts that are included in total balance calculation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts included in balance"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<AccountListDTO>> getAccountsIncludedInBalance();

    @Operation(
            summary = "Get total balance",
            description = "Calculate total balance across all accounts included in balance calculation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total balance"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalBalance();

    @Operation(
            summary = "Get total balance by type",
            description = "Calculate total balance for a specific account type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total balance by type"),
            @ApiResponse(responseCode = "400", description = "Invalid account type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<BigDecimal> getTotalBalanceByType(
            @Parameter(description = "Account type", required = true) String type);

    @Operation(
            summary = "Get account by ID",
            description = "Retrieve a specific account by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<AccountDto> getAccountById(
            @Parameter(description = "Account ID", required = true) Long id);

    @Operation(
            summary = "Search accounts by name",
            description = "Search accounts by name containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<AccountListDTO>> searchAccountsByName(
            @Parameter(description = "Name pattern to search for") String name);

    @Operation(
            summary = "Search active accounts by name",
            description = "Search active accounts by name containing the provided text"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching active accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<AccountListDTO>> searchActiveAccountsByName(
            @Parameter(description = "Name pattern to search for") String name);

    @Operation(
            summary = "Create a new account",
            description = "Create a new account with initial balance"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<AccountDto> createAccount(
            @Parameter(description = "Account creation request") AccountCreateRequestDto request);

    @Operation(
            summary = "Update account",
            description = "Update an existing account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or duplicate name"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<AccountDto> updateAccount(
            @Parameter(description = "Account ID", required = true) Long id,
            AccountUpdateRequestDto request);

    @Operation(
            summary = "Update account balance",
            description = "Update the balance of a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account balance updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<AccountDto> updateAccountBalance(
            @Parameter(description = "Account ID", required = true) Long id,
            @Parameter(description = "New balance") BigDecimal balance);

    @Operation(
            summary = "Add amount to account balance",
            description = "Add a specific amount to an account's balance"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amount added successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<AccountDto> addToAccountBalance(
            @Parameter(description = "Account ID", required = true) Long id,
            @Parameter(description = "Amount to add") BigDecimal amount);

    @Operation(
            summary = "Subtract amount from account balance",
            description = "Subtract a specific amount from an account's balance"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amount subtracted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<AccountDto> subtractFromAccountBalance(
            @Parameter(description = "Account ID", required = true) Long id,
            @Parameter(description = "Amount to subtract") BigDecimal amount);

    @Operation(
            summary = "Delete account",
            description = "Delete an account from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Account ID", required = true) Long id);
}

