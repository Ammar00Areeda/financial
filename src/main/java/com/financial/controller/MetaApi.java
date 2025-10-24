package com.financial.controller;

import com.financial.dto.MetaDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * API interface for Meta operations.
 * Contains all OpenAPI documentation for meta endpoints.
 */
@Tag(name = "Meta", description = "Application metadata and statistics operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface MetaApi {

    @Operation(
            summary = "Get application metadata",
            description = "Retrieve application statistics and metadata including counts and last sync time"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved metadata"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<MetaDto> getMeta();
}




