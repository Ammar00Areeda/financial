package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Transaction API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    
    private List<TransactionDto> transactions;
    private FiltersDto filters;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDto {
        private String id;
        private String date;
        private String accountId;
        private String accountName;
        private String categoryId;
        private String categoryName;
        private String description;
        private BigDecimal amount;
        private String type;
        private String currency;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FiltersDto {
        private String month;
        private List<String> accountIds;
        private List<String> categoryIds;
        private String type;
    }
}
