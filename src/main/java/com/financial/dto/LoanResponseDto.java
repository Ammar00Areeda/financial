package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Loan API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponseDto {
    
    private List<LoanDto> loans;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanDto {
        private String id;
        private String name;
        private String direction;
        private String counterparty;
        private String accountId;
        private String accountName;
        private BigDecimal amount;
        private String currency;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        private String notes;
    }
}
