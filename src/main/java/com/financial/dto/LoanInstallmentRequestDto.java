package com.financial.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating a loan installment payment.
 * Used when a user makes a payment towards their loan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanInstallmentRequestDto {
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
    
    @NotNull(message = "Paid date is required")
    private LocalDateTime paidAt;
}
