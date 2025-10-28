package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Dashboard Total API response.
 * Contains total financial position including accounts and outstanding loans.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTotalDto {
    
    private BigDecimal total;
    private String currency;
    private LocalDateTime lastUpdated;
    private OutstandingLoansDto outstandingLoans;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutstandingLoansDto {
        private BigDecimal given;
        private BigDecimal received;
    }
}
