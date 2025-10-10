package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Meta API response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaDto {
    
    private Long accountsCount;
    private Long transactionsCount;
    private Long loanCount;
    private Long categoryCount;
    private LocalDateTime lastSyncedAt;
}
