package com.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for loan installment payment response.
 * Contains all information about a processed installment payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanInstallmentResponseDto {
    
    private Long installmentId;
    private Long loanId;
    private Long accountId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paidAt;
    private String note;
    private BigDecimal remainingBalance;
    private LocalDateTime createdAt;
    private String status;
}
