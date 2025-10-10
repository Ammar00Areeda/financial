package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Account API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {
    
    private List<AccountDto> accounts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDto {
        private String id;
        private String name;
        private String type;
        private BigDecimal balance;
        private String currency;
        private String institution;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
