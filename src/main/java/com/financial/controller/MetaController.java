package com.financial.controller;

import com.financial.dto.MetaDto;
import com.financial.service.AccountService;
import com.financial.service.CategoryService;
import com.financial.service.LoanService;
import com.financial.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST controller for Meta operations.
 * Implements MetaApi interface which contains all OpenAPI documentation.
 */
@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaController implements MetaApi {

    private final AccountService accountService;
    private final CategoryService categoryService;
    private final LoanService loanService;
    private final TransactionService transactionService;

    @Override
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
