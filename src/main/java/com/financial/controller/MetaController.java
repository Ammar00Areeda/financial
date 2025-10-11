package com.financial.controller;

import com.financial.dto.MetaDto;
import com.financial.service.AccountService;
import com.financial.service.CategoryService;
import com.financial.service.LoanService;
import com.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST controller for Meta operations.
 */
@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
@Tag(name = "Meta", description = "Application metadata and statistics operations")
@SecurityRequirement(name = "Bearer Authentication")
public class MetaController {

    private final AccountService accountService;
    private final CategoryService categoryService;
    private final LoanService loanService;
    private final TransactionService transactionService;

    @Operation(
            summary = "Get application metadata",
            description = "Retrieve application statistics and metadata including counts and last sync time"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved metadata"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<MetaDto> getMeta() {
        
        MetaDto meta = MetaDto.builder()
                .accountsCount((long) accountService.getAllAccounts().size())
                .transactionsCount((long) transactionService.getAllTransactions().size())
                .loanCount((long) loanService.getAllLoans().size())
                .categoryCount((long) categoryService.getAllCategories().size())
                .lastSyncedAt(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(meta);
    }
}
