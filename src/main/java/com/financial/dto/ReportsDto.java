package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Reports API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsDto {
    
    private List<MonthlyReportDto> reports;
    private TotalsDto totals;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyReportDto {
        private String id;
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal savings;
        private String currency;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalsDto {
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal savings;
        private String currency;
    }
}
