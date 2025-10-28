package com.financial.controller;

import com.financial.dto.DashboardDto;
import com.financial.dto.DashboardTotalDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * API interface for Dashboard operations.
 * Contains all OpenAPI documentation for dashboard endpoints.
 */
@Tag(name = "Dashboard", description = "Dashboard data and overview operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface DashboardApi {

    @Operation(
            summary = "Get dashboard data",
            description = "Retrieve comprehensive dashboard data including net worth, accounts, recent transactions, active loans, and monthly spending"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<DashboardDto> getDashboard();

    @Operation(
            summary = "Get total financial position",
            description = "Calculate total financial position including account balances and outstanding loans"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated total financial position"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<DashboardTotalDto> getTotal();
}




