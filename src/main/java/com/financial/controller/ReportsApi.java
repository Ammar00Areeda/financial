package com.financial.controller;

import com.financial.dto.ReportsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * API interface for Reports operations.
 * Contains all OpenAPI documentation for reports endpoints.
 */
@Tag(name = "Reports", description = "Financial reports and analytics operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface ReportsApi {

    @Operation(
            summary = "Get monthly reports",
            description = "Retrieve monthly financial reports for the specified date range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly reports"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ReportsDto> getMonthlyReports(
            @Parameter(description = "Start date in YYYY-MM format", required = true) String start,
            @Parameter(description = "End date in YYYY-MM format", required = true) String end);
}




